package com.stock.model;

import com.stock.data.stockprice.StockPriceWareHouse;
import com.stock.model.tb.Peak;
import com.stock.model.tb.TbUtil;
import com.stock.model.tb.Trend;
import com.stock.model.tb.Zs;
import com.stock.util.DateUtil;
import com.stock.util.Params;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import util.BeanFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lll on 2016/5/24.
 */
public class TBModel implements StockModel{
    private static Logger logger = Logger.getLogger("TBModel");

	private String stock_id;
	private String date;
	//保存买入的股票当时对应的中枢高点，后续如果跌破这个高点，则卖出
	private  Map bought = new HashMap();

	public static void main(String[] args) {
		Params.dateBegin="20160201";
		Params.dateEnd="20160601";
		DateUtil.initDateMap(10);
		DateUtil.init30timeMap(10);
		TBModel tb = new TBModel();
		tb.date="20160421";
		tb.stock_id="603999";
		if(tb.hasBuy()){
			logger.info("true");
		};


	}

	private static void getZSList(List<Trend> trendList, TbUtil tu, List<Zs> zsListZM) {
		boolean isBreak=true;
		for (int i = 0; i < trendList.size(); i++) {
            //isbreak说明当前的中枢断档 需要从头开始计算是否有中枢
            if(isBreak){
                if(i+2<trendList.size()){
                    Trend thisTrend = (Trend) trendList.get(i);
                    Trend nextTrend = (Trend) trendList.get(i+1);
                    Trend nNextTrend = (Trend) trendList.get(i+2);
                    Zs smallZs = tu.hasZs(thisTrend, nextTrend, nNextTrend);
                    if(smallZs!=null){
                        zsListZM.add(smallZs);
                        isBreak=false;
                        i=i+2;
                    }
                }
            }else {
                //当前有中枢，首先判断接下来这个trend是否中枢延续
                if (zsListZM.size() > 0) {
                    Zs zs = zsListZM.get(zsListZM.size() - 1);
                    Trend thisTrend = (Trend) trendList.get(i);
                    double lowPrice = thisTrend.getLowPrice();
                    double highPrice = thisTrend.getHighPrice();
                    if(lowPrice>zs.getZsHighPrice()||highPrice<zs.getZsLowPrice()){
                        //不是中枢延续，判断是否自己形成中枢,可以将isbreak弄成true,然后i--,返回上面重新计算中枢
                        isBreak=true;
                        i--;
                    }else{
                        //中枢扩展
                        zs.setEndDate(thisTrend.getEndDate());
                        if(lowPrice>zs.getZsLowPrice())
                            zs.setZsLowPrice(lowPrice);
                        if(highPrice<zs.getZsHighPrice())
                            zs.setZsHighPrice(highPrice);
                        isBreak=false;
                    }
                }
            }
        }
		for (int i = 0; i < zsListZM.size(); i++) {
			Zs zs =  zsListZM.get(i);
//			logger.info("zm:"+zs);
		}
	}

	private static void getTrendList(List<Peak> peaks, List<Trend> trendList) {
		for (int i = 0; i < peaks.size() - 1; i++) {
            Peak peak = (Peak) peaks.get(i);
            Peak nextPeak = (Peak) peaks.get(i + 1);
            Trend trend = new Trend(peak.date, nextPeak.date,
                    peak.price <= nextPeak.price ? peak.price : nextPeak.price,
                    peak.price >= nextPeak.price ? peak.price : nextPeak.price,
                    peak.price > nextPeak.price ? false : true, 0);
            trendList.add(trend);
        }
		for (int i = 0; i < trendList.size(); i++) {
			Trend zs =  trendList.get(i);
//			logger.info("zmtrend;" + zs);
		}
	}

	private static void printPeak(List<Peak> peaks) {
		for (int p = 0; p < peaks.size(); p++) {
			Peak peak1 =  peaks.get(p);
//			logger.info(peak1);
		}
	}

	private static void printLog(List priceList) {
		for (int i = 0; i < priceList.size(); i++) {
            Map o = (Map) priceList.get(i);
            System.out.println(o.get("d_date"));
        }
	}

	/*
	 *将相邻高峰之间间隔天数小于2天的去除，因为有公用k线,再将相邻的高高取最高的，低低取最低的
	 */
	public static List<Peak> standardPeak(List<Peak> peak,String stock_id){
		for (int i = 0; i < peak.size()-1; i++) {
			Peak thisPeak =  peak.get(i);
			Peak nextPeak =  peak.get(i+1);
			if(DateUtil.getRange(stock_id, thisPeak.getDate(), nextPeak.getDate()).size()<4){
				peak.remove(i+1);
			}
		}
		for (int i = 0; i < peak.size()-1; i++) {
			Peak thisPeak =  peak.get(i);
			Peak nextPeak =  peak.get(i+1);
			if(thisPeak.isHigh==nextPeak.isHigh&&thisPeak.isHigh==true){
				if(thisPeak.getPrice()>=nextPeak.getPrice()){
					peak.remove(i+1);
				}else{
					peak.remove(i);
				}
				i--;
			}
			if(thisPeak.isHigh==nextPeak.isHigh&&thisPeak.isHigh==false){
				if(thisPeak.getPrice()<=nextPeak.getPrice()){
					peak.remove(i+1);
				}else{
					peak.remove(i);
				}
				i--;
			}
		}
		return peak;
	}
	/*
	 *将相邻高峰之间间隔天数小于2天的去除，因为有公用k线,再将相邻的高高取最高的，低低取最低的
	 * 这个为30分钟
	 */
	public static List<Peak> standard30Peak(List<Peak> peak,String stock_id){
		for (int i = 0; i < peak.size()-1; i++) {
			Peak thisPeak =  peak.get(i);
			Peak nextPeak =  peak.get(i+1);
			if(DateUtil.get30KRange(stock_id, thisPeak.getDate(), nextPeak.getDate()).size()<4){
				peak.remove(i+1);
			}
		}
		for (int i = 0; i < peak.size()-1; i++) {
			Peak thisPeak =  peak.get(i);
//			logger.info(thisPeak.getDate());
			Peak nextPeak =  peak.get(i+1);
			if(thisPeak.isHigh==nextPeak.isHigh&&thisPeak.isHigh==true){
				if(thisPeak.getPrice()>=nextPeak.getPrice()){
					peak.remove(i+1);
				}else{
					peak.remove(i);
				}
				i--;
			}
			if(thisPeak.isHigh==nextPeak.isHigh&&thisPeak.isHigh==false){
				if(thisPeak.getPrice()<=nextPeak.getPrice()){
					peak.remove(i+1);
				}else{
					peak.remove(i);
				}
				i--;
			}
		}
		return peak;
	}
    //list中的类型为数组，
	//获取list中的Peak高点和Peak低点，highPos和lowPos代表的是在数组中，高点和低点的位置
	//使用回测框架的时候需要调整
	public static List<Peak> getPeak(List al){
		ArrayList<Peak> result = new ArrayList<Peak>();
		for(int i=1;i<al.size()-1;i++){
			Map lastRow = (Map) al.get(i-1);
			Map thisRow = (Map) al.get(i);
			Map nextRow = (Map) al.get(i+1);
			BigDecimal comp1High =(BigDecimal) lastRow.get("high");
			BigDecimal comp1Low = (BigDecimal) lastRow.get("low");
			BigDecimal comp2High =(BigDecimal) thisRow.get("high");
			BigDecimal comp2Low = (BigDecimal) thisRow.get("low");
			BigDecimal comp3High = (BigDecimal) nextRow.get("high");
			BigDecimal comp3Low = (BigDecimal) nextRow.get("low");
			if(comp1High.floatValue()<=comp2High.floatValue()&&comp2High.floatValue()>=comp3High.floatValue()){
				Peak peak = new Peak((String) thisRow.get("d_date"), comp2High.doubleValue(), true);
				result.add(peak);
			}
			if(comp1Low.floatValue()>=comp2Low.floatValue()&&comp2Low.floatValue()<=comp3Low.floatValue()){
				Peak peak = new Peak((String) thisRow.get("d_date"), comp2Low.doubleValue(), false);
				result.add(peak);
			}
		}
		return result;
	}
	public static List<Peak> get30Peak(List al){
		ArrayList<Peak> result = new ArrayList<Peak>();
		//之前日线上出现转折，再取转折点来判断30分钟线，所以需要在首尾各增加一个peak
		//20160605 其中头上加peak没问题，尾部加peak，需要该日最后一个30k为最低点，否则不加
		Map firstMap = (Map) al.get(0);
		BigDecimal firstHigh =(BigDecimal) firstMap.get("high");
		Peak firstPeak = new Peak((String)firstMap.get("d_date"),firstHigh.doubleValue(),true);
		result.add(firstPeak);
		double lowest = 999;

		for(int i=1;i<al.size()-1;i++){
			Map lastRow = (Map) al.get(i-1);
			Map thisRow = (Map) al.get(i);
			Map nextRow = (Map) al.get(i+1);
			BigDecimal comp1High =(BigDecimal) lastRow.get("high");
			BigDecimal comp1Low = (BigDecimal) lastRow.get("low");
			BigDecimal comp2High =(BigDecimal) thisRow.get("high");
			BigDecimal comp2Low = (BigDecimal) thisRow.get("low");
			BigDecimal comp3High = (BigDecimal) nextRow.get("high");
			BigDecimal comp3Low = (BigDecimal) nextRow.get("low");
			if(comp1High.floatValue()<=comp2High.floatValue()&&comp2High.floatValue()>=comp3High.floatValue()){
				Peak peak = new Peak((String) thisRow.get("d_date"), comp2High.doubleValue(), true);
				result.add(peak);
			}
			if(comp1Low.floatValue()>=comp2Low.floatValue()&&comp2Low.floatValue()<=comp3Low.floatValue()){
				Peak peak = new Peak((String) thisRow.get("d_date"), comp2Low.doubleValue(), false);
				lowest = lowest<comp2Low.doubleValue()?lowest:comp2Low.doubleValue();

				result.add(peak);
			}
		}
		Map lastMap = (Map) al.get(al.size()-1);
		BigDecimal lastLow =(BigDecimal) lastMap.get("low");
		if(lastLow.doubleValue()<lowest){
			Peak lastPeak = new Peak((String)lastMap.get("d_date"),lastLow.doubleValue(),false);
			result.add(lastPeak);
		}
		return result;
	}
    //判断comp1和comp2是否是包含关系，highpos和lowpos代表了这个数组里面高点和低点在哪个位置
	//这个函数使用回测框架的时候，需要调整
	public static Boolean isContain(Map comp1,Map comp2){
		Boolean result = false;
		BigDecimal comp1High = (BigDecimal) comp1.get("high");
		BigDecimal comp1Low = ((BigDecimal) comp1.get("low"));
		BigDecimal comp2High = ((BigDecimal) comp2.get("high"));
		BigDecimal comp2Low = ((BigDecimal) comp2.get("low"));
		if(comp1High.floatValue()<=comp2High.floatValue() && comp1Low.floatValue()>=comp2Low.floatValue()){
			result = true;
		}
		return result;
	}
    /*
	 * 第一次清理数据：主要对日K线进行清理
	 * 清理内容：去除包含关系的日K，比如前一天高点和低点都在后一天中间，则前一天的数据可以排除
	 * 返回k线，去除包含关系
	 */
	public static List firstClean(List al){
		for(int i=1;i<al.size()-1;i++){
			Map lastRow = (Map) al.get(i-1);
			Map thisRow = (Map) al.get(i);
			Map nextRow = (Map) al.get(i+1);
			if(isContain(thisRow, lastRow)){
				al.remove(i);
				i--;
			}
			if(!isContain(thisRow, lastRow)&&isContain(thisRow, nextRow)){
				al.remove(i);
				i--;
			}
		}
		return al;
	}

	@Override
	public boolean hasSell(float costPrice) {
		List result = StockPriceWareHouse.getStockInfoList(
				stock_id,  new ArrayList(){{ add(date);}});
		if(result==null)
			return false;
		Map m = (Map) result.get(0);
		BigDecimal close = (BigDecimal) m.get("close");
		Double zsHigh = (Double) bought.get(stock_id);
		if(close.doubleValue()<zsHigh.doubleValue()){
			bought.remove(stock_id);
			return true;
		}
		if((close.floatValue())/costPrice>Params.profitPer){
			bought.remove(stock_id);
			return true;
		}
//		if((close.floatValue())/costPrice<Params.stopLossPer){
//			bought.remove(stock_id);
//			return true;
//		}
		return false;
	}

	@Override
	public void setDate(String date) {
		this.date = date;

	}

	@Override
	public void setStock(String stock) {
		this.stock_id = stock;
	}

	@Override
	public boolean hasBuy() {
		//买过的就不买了
		if(bought.containsKey(stock_id))
			return false;
        JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean("jdbcTemplate");
		List priceList = StockPriceWareHouse.getStockInfoList(
				stock_id, DateUtil.getRange(stock_id, DateUtil.getBackdate(date,this.getBackDayCount(),stock_id), date));

		//合并k线
		firstClean(priceList);

//		printLog(priceList);
		//计算高低点
		List<Peak> peak0 = getPeak(priceList);

		printPeak(peak0);
		//相邻高低点如果间隔不大于2个时间间隔，则合并
		if(peak0.size()<2){
			return false;
		}
		List<Peak> peaks = standardPeak(peak0, stock_id);
		printPeak(peaks);


		List<Trend> trendList = new ArrayList<Trend>();
		TbUtil tu = new TbUtil();
		//trend就是高点和低点相连的笔
		getTrendList(peaks, trendList);
		for (int i = 0; i < trendList.size(); i++) {
			Trend trend =  trendList.get(i);
//			logger.info(trend);
		}

        //中枢，三笔重合部分构成中枢，后续一笔只要与中枢有交集，即可算作中枢延续
        List<Zs> zsListZM = new ArrayList<Zs>();
		getZSList(trendList, tu, zsListZM);
		if(zsListZM.size()==0){
			return false;
		}
		Zs lastZs = zsListZM.get(zsListZM.size()-1);
		//三买，最后一个笔是上涨，然后有次级别的回抽不破中枢高点,切次级别下上下出现背离
		Trend lastTrend = trendList.get(trendList.size() - 1);
		String endDate = lastTrend.getEndDate();
		String lastZsEndDate = lastZs.getEndDate();
		//如果最后一笔的开始日期与最近一个中枢的结束日期不同，说明中枢后有断点，且尚未形成中枢
//		logger.info("endDate:"+endDate+ " lastZsEndDate:"+lastZsEndDate);
		if(!endDate.equals(lastZsEndDate))
			return false;
		if(lastTrend.getIsRise()){
			String beginDate = lastTrend.getBeginDate();
			String dateBegFor30 = endDate;
			List<String> range = DateUtil.getRange(stock_id, beginDate, dateBegFor30);
			//最后一个拉升超过5天则剔除。
			//附加条件
//			if(range.size()>5)
//				return false;
			double lastUpPer= lastTrend.getHighPrice() / lastTrend.getLowPrice();
			//附加条件
//			if(lastUpPer<1.1||lastUpPer>1.35){
//				logger.debug("最后一笔突破不超过20%，则不买入");
//				return false;
//			}
			//判断从date到今天，是否形成30分钟线的下上下，且背离
//			List<Map<String, Object>> infoThirty = jdbc.queryForList("select t_time d_date,open,close,high,low,macd" +
//					" from stock_thirty where stock_id=? and date_format(t_time,'%Y%m%d') between ? and ? " +
//					"order by d_date "
//					, new Object[]{stock_id, dateBegFor30, date});
			String dateBeg10 = dateBegFor30.substring(0,4)+"-"+dateBegFor30.substring(4,6)+"-"+dateBegFor30.substring(6,8);
			String dateEnd10 = date.substring(0,4)+"-"+date.substring(4,6)+"-"+date.substring(6,8);
			List<Map<String, Object>> infoThirty = StockPriceWareHouse.getStockThirtyInfoList(stock_id, DateUtil.get30KRange(stock_id,dateBeg10,dateEnd10) );
//			logger.info(infoThirty);
//			logger.info(infoThirty.size());
			if(infoThirty.size()==0) {
				logger.debug(dateBegFor30 + "没有30分钟k线 ");
				return false;
			}
			firstClean(infoThirty);
//			printLog(infoThirty);

			List<Peak> peak30 = get30Peak(infoThirty);
//			logger.info(stock_id+"+"+ dateBegFor30+"+"+ date);
//			printPeak(peak30);
			List<Peak> peakStd30 = standard30Peak(peak30, stock_id);
			printPeak(peakStd30);

			List<Trend> trend30List = new ArrayList<Trend>();
			//trend就是高点和低点相连的笔
			getTrendList(peakStd30, trend30List);
			if(trend30List.size()==3){
				Trend trend1 = trend30List.get(0);
				Trend trend2 = trend30List.get(1);
				Trend trend3 = trend30List.get(2);
				//三买逻辑：1:下上下，最后一笔的macd比之前的低点小;2不破之前中枢高点 3,后一笔低点低于第一笔低点
				double firlowPrice = trend1.getLowPrice();
				double seclowPrice = trend3.getLowPrice();
				String dateD1 = trend1.getEndDate();
				String dateD2 = trend3.getEndDate();
				Map<String, Object> d1Map = (Map<String, Object>) StockPriceWareHouse.getStockThirtyInfoByTime(stock_id,dateD1);
				Map<String, Object> d2Map = (Map<String, Object>) StockPriceWareHouse.getStockThirtyInfoByTime(stock_id,dateD2);
//				List<Map<String, Object>> d1Map = jdbc.queryForList("select macd ,t_time d_date from stock_thirty where stock_id = ? and t_time =? ",
//						new Object[]{stock_id, dateD1});
//				List<Map<String, Object>> d2Map = jdbc.queryForList("select macd ,t_time d_date from stock_thirty where stock_id = ? and t_time =? ",
//						new Object[]{stock_id, dateD2});
				BigDecimal macd1 = (BigDecimal) d1Map.get("macd");
				BigDecimal macd2 = (BigDecimal) d2Map.get("macd");
				//后低macd是前低的5一半
				//附加条件
				if(firlowPrice>seclowPrice){
					if(macd1.floatValue()>0&&macd2.floatValue()<0)
						return false;
                    if(macd2.abs().floatValue()<macd1.abs().floatValue()/1.5){
                        if(seclowPrice>lastZs.getZsHighPrice()){
                            bought.put(stock_id,lastZs.getZsHighPrice());
                            return true;
                        }
                    }
					bought.put(stock_id,lastZs.getZsHighPrice());
					return true;
				}

			}
		}
		return false;
	}

	@Override
	public int getBackDayCount() {
		return 60;
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
