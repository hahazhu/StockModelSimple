package com.stock.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

import com.stock.data.stockprice.StockPriceWareHouse;
import com.stock.model.tb.Peak;
import com.stock.model.tb.TbUtil;
import com.stock.model.tb.Trend;
import com.stock.model.tb.Zs;
import com.stock.util.DateUtil;
import com.stock.util.Params;

public class ThirdBuyModel implements StockModel {

    JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean("jdbcTemplate");
    Logger logger = Logger.getLogger("TrendBuyModel");
    TbUtil tu = new TbUtil();
    ArrayList<String[]> row = new ArrayList<String[]>();
    ArrayList<Peak> peakList = new ArrayList<Peak>();
    Peak lastPeak = new Peak();
    ArrayList<Trend> trendList = new ArrayList<Trend>();
    ArrayList zsList = new ArrayList();
    Zs bigZs = new Zs();
    ArrayList<Zs> bigZsList = new ArrayList<Zs>();
    private String stockId;
    private String date;

    @Override
    public String toString() {
        return "ThirdBuyModel [stockId=" + stockId + ", date=" + date + "]";
    }

    public static void main(String[] args) {
        Params.dateBegin = "20160101";
        Params.dateEnd = "20160522";
        ThirdBuyModel tb = new ThirdBuyModel();
        DateUtil.initDateMap(tb.getBackDayCount());
        StockPriceWareHouse.setBackCount(tb.getBackDayCount());
        tb.setDate("20160509");
        tb.setStock("600131");
        tb.hasBuy();

    }

    public void stockAnalysis() {
        logger.debug("datebegin:"+DateUtil.getBackdate(date, Params.TB_N_DAYS, stockId)+" dateEnd:"+date);

        List result = StockPriceWareHouse.getStockInfoList(stockId,
                DateUtil.getRange(stockId, DateUtil.getBackdate(date, Params.TB_N_DAYS, stockId), date));

        for (Iterator<Map<String, Object>> it = result.iterator(); it.hasNext(); ) {
            Map dataRow = it.next();
            logger.debug(dataRow);
            String[] rowArr = new String[6];
            rowArr[0] = dataRow.get("d_date").toString();
            rowArr[1] = dataRow.get("open").toString();
            rowArr[2] = dataRow.get("high").toString();
            rowArr[3] = dataRow.get("close").toString();
            rowArr[4] = dataRow.get("low").toString();
            rowArr[5] = dataRow.get("macd").toString();
            row.add(rowArr);
        }
        if (row.size() < Params.TB_N_VALID_DAYS) {
            return;
        }
        // 对于K线做第一次处理
        tu.firstClean(row);
        /*
		 * 获取峰值，峰值的定义详见峰值类中的说明 补充当前收盘价为最新一个峰值。（如果前一个是高，这个就为低，反之相反）
		 */
        peakList = tu.getPeak(row, 2, 4);
        lastPeak = (Peak) peakList.get(peakList.size() - 1);
        if (lastPeak.isHigh) {
            String[] temp = (String[]) row.get(row.size() - 1);
            Peak virtualPeak = new Peak(temp[0], Double.parseDouble(temp[3]), false);
            peakList.add(virtualPeak);
        } else {
            String[] temp = (String[]) row.get(row.size() - 1);
            Peak virtualPeak = new Peak(temp[0], Double.parseDouble(temp[3]), true);
            peakList.add(virtualPeak);
        }
        for (int i = 0; i < peakList.size(); i++) {
            Peak temp = (Peak) peakList.get(i);
            logger.debug(temp.getDate() + "," + temp.getPrice() + "," + temp.getIsHigh());
        }
        logger.debug("--------------------------完美分割线---------------------------");
        // 第二次清理：本次主要针对于Peak做清理
        peakList = tu.secondClean(peakList);
        for (int i = 0; i < peakList.size(); i++) {
            Peak temp = (Peak) peakList.get(i);
            logger.debug(temp.getDate() + "," + temp.getPrice() + "," + temp.getIsHigh());
        }
        logger.debug("--------------------------完美分割线---------------------------");
		/*
		 * 两个Peak必然形成一个趋势 取到每个趋势，加入趋势列表 记录每个趋势的macd汇总，用于以后判断背离用
		 */

        for (int i = 0; i < peakList.size() - 1; i++) {
            Peak peak = (Peak) peakList.get(i);
            Peak nextPeak = (Peak) peakList.get(i + 1);
            double macdVol = 0;
            Trend trend = new Trend(peak.date, nextPeak.date,
                    peak.price <= nextPeak.price ? peak.price : nextPeak.price,
                    peak.price >= nextPeak.price ? peak.price : nextPeak.price,
                    peak.price > nextPeak.price ? false : true, 0);
            for (int j = 0; j < row.size(); j++) {
                String[] temp = (String[]) row.get(j);
                if ((temp[0].compareTo(trend.getBeginDate()) >= 0) && (temp[0].compareTo(trend.getEndDate()) <= 0)) {
                    if ((trend.getIsRise()) && (Double.parseDouble(temp[5]) > 0)) {
                        macdVol += Double.parseDouble(temp[5]);
                    }
                    if ((!trend.getIsRise()) && (Double.parseDouble(temp[5]) < 0)) {
                        macdVol += Double.parseDouble(temp[5]);
                    }

                }
            }
            trend.setMacdVol(macdVol);
            trendList.add(trend);
        }
		/*
		 * 获取每三个趋势的重叠部分，如有，就是一个小中枢 倒序处理，每每三个趋势去比较
		 */

        boolean hasQs = false;// 上个中枢是否已经有对应的趋势了。
        boolean hasZs = false;// 是否之前有一个中枢了
        Trend lastTP = new Trend();// 寻找最后一个突破趋势，为了在最后一个中枢中把这个趋势删除
        for (int i = trendList.size() - 1; i > 0; i--) {
            if ((i - 2) >= 0) {
                Trend thisTrend = (Trend) trendList.get(i - 2);
                Trend nextTrend = (Trend) trendList.get(i - 1);
                Trend nNextTrend = (Trend) trendList.get(i);
                Zs smallZs = tu.hasZs(thisTrend, nextTrend, nNextTrend);
                // 如果zs不是空，代表是有小中枢的
                // 小中枢之间如果有重叠需要用comZs来扩展
                if (smallZs != null) {
                    smallZs.setZsTrend(null);
                    smallZs.setFirstTrend(thisTrend);
                    smallZs.setSecondTrend(nextTrend);
                    smallZs.setThirdTrend(nNextTrend);
                    zsList.add(smallZs);
                    if (hasZs) {
                        if (tu.comZs(smallZs, bigZs) == null) {
                            bigZs.setZsTrend(nextTrend);
                            bigZsList.add(bigZs);
                            bigZs = new Zs();
                            hasZs = false;
                            continue;
                        } else {
                            bigZs = tu.comZs(smallZs, bigZs);
                        }
                    } else {
                        bigZs = smallZs;
                    }
                    hasZs = true;
                }
                // 如果这三个趋势不形成中枢，第二个趋势肯定是一个真正的上涨或者下跌趋势
                // 一旦出现这种情况，之前一个大中枢就形成了（当然，如果第一个循环就不形成中枢，之前就不能形成大中枢了）
                else {
                    if (i != trendList.size() - 1) {
                        bigZs.setZsTrend(nextTrend);
                        bigZsList.add(bigZs);
                        bigZs = new Zs();
                        hasZs = false;
                    }
                    if (i == trendList.size() - 1) {
                        lastTP = nextTrend;// 那么倒数第二个趋势就是最后的突破趋势
                    }

                }
            }
        }
        // 时间结束了，但是还有没纳入bigZs的smallZs，纳入
        if (bigZs.getBeginDate() != null) {
            bigZsList.add(bigZs);
        }
        // 最后一个趋势是突破趋势
        if (trendList.size() > 0 && bigZsList.size() > 0) {
            if ((trendList.get(trendList.size() - 1).getHighPrice() == bigZsList.get(bigZsList.size() - 1)
                    .getHighPrice())
                    || (trendList.get(trendList.size() - 1).getLowPrice() == bigZsList.get(bigZsList.size() - 1)
                    .getLowPrice())) {
                lastTP = trendList.get(trendList.size() - 1);
            }
        }
        logger.debug("lastTP: " + lastTP);
        for (int i = 0; i < bigZsList.size(); i++) {
            Zs zs = (Zs) bigZsList.get(i);
            logger.debug(zs.toString());
        }
        logger.debug("--------------------------完美分割线---------------------------");
		/*
		 * 上面得到的bigZsList是包含了首尾趋势的中枢。现在需要删除首尾趋势 删除首尾的算法详见Zs类
		 */
        ArrayList zsTrendList = new ArrayList();
        for (int i = 0; i < bigZsList.size(); i++) {
            bigZsList.get(i).delTrend(bigZsList.get(i).getZsTrend());
            if ((i >= 0) && (i < bigZsList.size() - 1)) {
                bigZsList.get(i + 1).delTrend(bigZsList.get(i).getZsTrend());
            }
			/* 删除最后一个突破趋势 */
            if ((i == 0) && (lastTP != null)) {
                bigZsList.get(i).delTrend(lastTP);
            }
        }
        zsList = bigZsList;
        for (int i = 0; i < bigZsList.size(); i++) {
            Zs zs = (Zs) bigZsList.get(i);
            logger.debug(zs.toString());
        }
    }

    @Override
    public boolean hasSell(float costPrice) {
        stockAnalysis();
        // 如果历史数据太少怎么办 ，是卖是留？
        if ((row.size() < Params.TB_N_VALID_DAYS) || (bigZsList.size() < 2)) {
            return Params.TB_SELL_INVALID_SELL;
        }
        Zs lastZs = bigZsList.get(0);// 倒数第一个中枢
        Zs last2Zs = bigZsList.get(1);// 倒数第二个中枢
        String[] lastRow = (String[]) row.get(row.size() - 1);// 最后一天数据
        Double lastClose = Double.parseDouble(lastRow[3]);// 最后一天收盘价
        Trend lastSecTrend = trendList.get(trendList.size() - 2);// 倒数第二个趋势

        Trend lastHighTrend = new Trend();// lastHighTrend：上一个上涨趋势
        Peak lastPeak = peakList.get(peakList.size() - 1);// 最后一个峰值
        // 判断最后一个上涨趋势是哪个
        if (trendList.get(trendList.size() - 1).getIsRise()) {
            lastHighTrend = trendList.get(trendList.size() - 1);
        } else {
            lastHighTrend = trendList.get(trendList.size() - 2);
        }
        if (((!(Params.TB_SELL_ZS_OR_MAX == 1) || ((lastZs.getHighPrice() * Params.TB_SELL_PERCENTS >= lastClose))) // 下跌时候跌破前高点的百分比
                && (!(Params.TB_SELL_ZS_OR_MAX == 2)
                || ((lastZs.getZsHighPrice() * Params.TB_SELL_PERCENTS >= lastClose)))// 下跌时候跌破前中枢的中枢高点的百分比
        ) || (!(Params.TB_SELL_BEILI)
                || (((!lastPeak.isHigh) && (lastHighTrend.getMacdVol() <= lastZs.getZsTrend().getMacdVol())
                && (lastHighTrend.getHighPrice() > lastZs.getHighPrice())))) // （前一天已经出现上涨的高点）出现背离：！lastPeak.isHigh说明已经有回落了
                ) {
            return true;
        }
        return false;
    }

    @Override
    public void setDate(String date) {
        // TODO Auto-generated method stub
        this.date = date;
    }

    @Override
    public void setStock(String stock) {
        // TODO Auto-generated method stub
        this.stockId = stock;
    }

    @Override
	/*
	 * 最终判断三买： 1、上一个中枢（前）的趋势是上涨的 2、今天的收盘价格高于上个中枢的高点 3、今天的收盘价低于上个Peak(上个Peak为高点）
	 * 4、这个趋势没有出现背离
	 */
    public boolean hasBuy() {
        stockAnalysis();
        // 如果历史数据太少，不买入
        if ((row.size() < Params.TB_N_VALID_DAYS) || (bigZsList.size() < 2)) {
            return false;
        }
        Zs lastZs = bigZsList.get(0);// 倒数第一个中枢
        Zs last2Zs = bigZsList.get(1);// 倒数第二个中枢
        String[] lastRow = (String[]) row.get(row.size() - 1);// 最后一天数据
        Double lastClose = Double.parseDouble(lastRow[3]);// 最后一天收盘价
        Double lastMacd = Double.parseDouble(lastRow[5]);// 最后一天MACD
        Trend lastSecTrend = trendList.get(trendList.size() - 2);// 倒数第二个趋势
        if (last2Zs.getZsTrend() != null) {
            if (((!true) || (lastZs.getZsTrend().getIsRise())) // 上一个zs前的趋势必须是上涨的（必须条件）
                    && ((!true) || (lastPeak.getPrice() * Params.TB_BACK_PERCENTS > lastClose)) // 当未实现下一级中枢判断之前（必须条件），计算最高点下来的比例
                    && ((!true) || (lastSecTrend.getMacdVol() > lastZs.getZsTrend().getMacdVol())) // 这次上涨突破趋势比上一个中枢前的上涨突破趋势相比未背离
                    && (!(Params.TB_ZS_OR_MAX == 1) || (lastZs.getHighPrice() < lastClose)) // 当前价要高于前中枢最高点
                    && (!(Params.TB_ZS_OR_MAX == 2) || (lastZs.getZsHighPrice() < lastClose)) // 当前价要高于前中枢的中枢最高点
                    && (!(Params.TB_FIRST_TB) || (!last2Zs.getZsTrend().getIsRise())) // 取第一个三买，即前一个中枢是上涨，再上一个中枢是下跌的
                    && (!(Params.TB_TREND_RISE_PERCENTS > 1) || (lastSecTrend
                    .getHighPrice() < lastSecTrend.getLowPrice() * Params.TB_TREND_RISE_PERCENTS)) // 这次上涨突破趋势的涨幅不能太大。
                    ) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getBackDayCount() {
        // TODO Auto-generated method stub
        return Params.TB_N_DAYS;
    }

    @Override
    public float getBuyPrice() {
        return 0;
    }

    @Override
    public float getSellPrice() {
        return 0;
    }

    @Override
    public String getBuyDate() {
        return null;
    }

    @Override
    public String getSellDate() {
        return null;
    }

    @Override
    public int getHoldPeriod() {
        return 0;
    }

    @Override
    public void setHoldPeriod(int holdPeriod) {

    }

}