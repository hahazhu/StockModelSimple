/**
 * 
 */
package com.stock.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

import com.stock.data.stockprice.StockPriceWareHouse;
import com.stock.util.DateUtil;
import com.stock.util.Params;
import com.stock.util.PriceUtil;

/**
 * @author hahazhu
 * 15个点止盈 10个点止损
 *
 */
public class TrendBuyV2Model implements StockModel {
	JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean(
		"jdbcTemplate");
	Logger logger = Logger.getLogger("TrendBuyModel");
	private String stockId;
	private String date;
	private int N_DAYS=5;
	private static Map middleLayerStock = new HashMap();

	/* (non-Javadoc)
	 * @see com.com.stock.model.StockModel#hasSell()
	 */
	@Override
	public boolean hasSell(float costPrice) {
		// TODO Auto-generated method stub
		List result = StockPriceWareHouse.getStockInfoList(
				stockId,  new ArrayList(){{ add(date);}});
		if(result==null)
			return false;
		Map m = (Map) result.get(0);
		BigDecimal close = (BigDecimal) m.get("close");
		if((close.floatValue())/costPrice>Params.profitPer){
			return true;
		}
		if((close.floatValue())/costPrice<Params.stopLossPer){
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.com.stock.model.StockModel#setDate(java.lang.String)
	 */
	@Override
	public void setDate(String date) {
		// TODO Auto-generated method stub
		this.date = date;
	}

	/* (non-Javadoc)
	 * @see com.com.stock.model.StockModel#setStock(java.lang.String)
	 */
	@Override
	public void setStock(String stock) {
		// TODO Auto-generated method stub
		this.stockId = stock;

	}

	/* (non-Javadoc)
	 * @see com.com.stock.model.StockModel#hasBuy()
	 */
	@Override
	public boolean hasBuy() {
		// TODO Auto-generated method stub
		//如果之前满足小碎步，5天内回撤到ma10，即买入。
		if(middleLayerStock.containsKey(stockId)){

			List stockInfo= StockPriceWareHouse.getStockInfoList(stockId, new ArrayList(){{
				add(date);
			}});
			Map m = (Map) stockInfo.get(0);
			BigDecimal low = (BigDecimal)m.get("low");
			BigDecimal ma10 = (BigDecimal)m.get("ma10");
			if(low.floatValue()<ma10.floatValue()){
				middleLayerStock.remove(stockId);
				return true;
			}else{
				String indate = (String) middleLayerStock.get(stockId);
				//当前日期回溯5个交易日，如果还没发生low<ma10。移除middlelayer，不买了。
				String back5date = DateUtil.getBackdate(date, 5, stockId);
				if(Integer.valueOf(back5date)>Integer.valueOf(indate)){
					middleLayerStock.remove(stockId);
				}
				return false;
			}
		}
		
		// 日k线
		List result = StockPriceWareHouse.getStockInfoList(
				stockId, DateUtil.getRange(stockId, DateUtil.getBackdate(date,N_DAYS,stockId), date));
		int n = 0; // 连续趋势的天数
		float last_volume = (float) 0;
		for (Iterator<Map<String, Object>> it = result.iterator(); it.hasNext();) {
			Map row = it.next();
			logger.debug(row);
			BigDecimal ma5 = (BigDecimal) row.get("MA5");
			BigDecimal ma10 = (BigDecimal) row.get("MA10");
			BigDecimal percent = (BigDecimal) row.get("PERCENT");
			BigDecimal volume = (BigDecimal) row.get("VOLUME");
			if (last_volume != 0) {
				// 小碎步条件：涨幅在2个点之内，成交量在前一日的90%到130%之间，均线向上；
				// 第一天无所谓
				if (PriceUtil.between(percent.floatValue(), (float) 1, (float) 4,
						(float) 0)
						&& PriceUtil.between(volume.floatValue(), last_volume,
								(float) 1.3, (float) 0.9)
						&& ma5.floatValue() > ma10.floatValue() && n > 0) {
					n++;
				} else if (PriceUtil.between(percent.floatValue(), (float) 1, (float) 3,
						(float) 0) && n == 0) {
					n++;
				} else if (PriceUtil.between(percent.floatValue(), (float) 1, (float) 3,
						(float) 0) && n > 0) {
					n = 1;
				} else {
					n = 0;
				}
			}
			last_volume = volume.floatValue();
		}
//		System.out.println("$$$$$$$$$$$$"+n);
		if (n == N_DAYS) {
			middleLayerStock.put(stockId, date);
			return false;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.com.stock.model.StockModel#getBackDayCount()
	 */
	@Override
	public int getBackDayCount() {
		// TODO Auto-generated method stub
		return N_DAYS+10;
	}

}
