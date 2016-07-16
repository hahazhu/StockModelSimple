/**
 * 
 */
package com.stock.model;

import java.math.BigDecimal;
import java.util.ArrayList;
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
 *
 */
public class TrendBuyModel implements StockModel {
	JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean(
		"jdbcTemplate");
	Logger logger = Logger.getLogger("TrendBuyModel");
	private String stockId;
	private String date;
	private int N_DAYS=5;

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
				if (PriceUtil.between(percent.floatValue(), (float) 1, (float) 3,
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
			return true;
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
