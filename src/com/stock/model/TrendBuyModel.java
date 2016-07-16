/**
 * 
 */
package com.stock.model;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

import com.stock.backtest.BacktestingFrame;
import com.stock.data.stockprice.StockPriceWareHouse;
import com.stock.util.DateUtil;
import com.stock.util.Params;
import com.stock.util.PriceUtil;

/**
 * @author hahazhu
 *
 */
public class TrendBuyModel implements StockModel {
	private  static JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean(
		"jdbcTemplate");
	Logger logger = Logger.getLogger("TrendBuyModel");
	private String stockId;
	private String date;
	private int N_DAYS=5;
	private Map<String,String> holdStock = new HashMap<String,String>();
	private Map<String,String> holdStockPrice = new HashMap<String,String>();
	private Map<String,String> preStock = new HashMap<String,String>();
	private float buyPrice;
	private float sellPrice;
	private String buyDate;
	private String sellDate;
	private  int holdPeriod;

	public static void signal_day_insert(final String stock_id ,final String xsb_date ,
										 final String this_date,final int day_passed  ,final double enter_price ,final String enter_date
										,final int enter_day_passed  ,final double ma5_price,final double ma10_price  ,final int status,
										 final double sell_price  ,final String sell_date,final double profit) throws SQLException {
		String sql_str ="delete from stock_xsb_result where stock_id ='"+stock_id+"' and xsb_date ='"+xsb_date+"'";
		jdbc.update(sql_str);
		sql_str = "insert into stock_xsb_result values('"+stock_id+"','"+xsb_date+"','"+this_date+"','"+day_passed+"','"+enter_price+"','"+enter_date+"','"+enter_day_passed+"','"+ma5_price+"','"+ma10_price+"','"+status+"','"+sell_price+"','"+sell_date+"','"+profit+"')";
		jdbc.update(sql_str);
	}

	/* (non-Javadoc)
	 * @see com.stock.model.StockModel#hasSell()
	 */
	@Override
	public boolean hasSell(float costPrice) {
		// TODO Auto-generated method stub
		String buydate = holdStock.get(stockId);
		List result = jdbc.queryForList("select distinct d_date from stock_day where d_date between ? and ?",
				new Object[]{buydate,date});
		if(result.size()>=holdPeriod) {
			float boughtPrice = Float.valueOf(holdStockPrice.get(stockId));
			List info = StockPriceWareHouse.getStockInfoList(stockId,new ArrayList(){{
				add(date);
			}});
			Map m = (Map)info.get(0);
			BigDecimal ma5 = (BigDecimal) m.get("MA5");
			BigDecimal low = (BigDecimal) m.get("LOW");
			BigDecimal close = (BigDecimal) m.get("CLOSE");
			if((close.floatValue()/boughtPrice)<1.08){
				holdStock.remove(stockId);
				holdStockPrice.remove(stockId);
				this.sellDate=date;
				return true;
			}
            if(close.floatValue()<ma5.floatValue()){
                holdStock.remove(stockId);
				holdStockPrice.remove(stockId);
                this.sellDate=date;
                return true;
            }
			return false;
		}else{
			return false;
		}
//		List result = StockPriceWareHouse.getStockInfoList(
//				stockId,  new ArrayList(){{ add(date);}});
//		if(result==null)
//			return false;
//		Map m = (Map) result.get(0);
//		BigDecimal close = (BigDecimal) m.get("close");
//		if((close.floatValue())/costPrice>Params.profitPer){
//			return true;
//		}
//		if((close.floatValue())/costPrice<Params.stopLossPer){
//			return true;
//		}
//		return false;
	}

	/* (non-Javadoc)
	 * @see com.stock.model.StockModel#setDate(java.lang.String)
	 */
	@Override
	public void setDate(String date) {
		// TODO Auto-generated method stub
		this.date = date;
	}

	/* (non-Javadoc)
	 * @see com.stock.model.StockModel#setStock(java.lang.String)
	 */
	@Override
	public void setStock(String stock) {
		// TODO Auto-generated method stub
		this.stockId = stock;

	}
	public float getBuyPrice(){
		return buyPrice;
	}
	public float getSellPrice(){
		return sellPrice;
	}
	public String getBuyDate(){
		return buyDate;
	}
	public String getSellDate(){
		return sellDate;
	}

	/* (non-Javadoc)
	 * @see com.stock.model.StockModel#hasBuy()
	 */
	@Override
	public boolean hasBuy() {
		// TODO Auto-generated method stub

		if(preStock.containsKey(stockId)){
			List info = StockPriceWareHouse.getStockInfoList(stockId,new ArrayList(){{
				add(date);
			}});
			Map m = (Map)info.get(0);
			BigDecimal ma5 = (BigDecimal) m.get("MA5");
			BigDecimal ma10 = (BigDecimal)m.get("MA10");
			BigDecimal low = (BigDecimal) m.get("LOW");
			if(low.floatValue()<((ma5.floatValue()+ma10.floatValue())/2)){
				preStock.remove(stockId);
				this.buyDate=date;
				this.buyPrice=(ma5.floatValue()+ma10.floatValue())/2;
				holdStock.put(stockId,date);
				holdStockPrice.put(stockId,""+buyPrice);
				return true;
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
				if (PriceUtil.between(percent.floatValue(), (float) 1, (float) 3.5,
						(float) 0)
						&& PriceUtil.between(volume.floatValue(), last_volume,
								(float) 1.8, (float) 0.9)
						&& ma5.floatValue() > ma10.floatValue() && n > 0) {
					n++;
				} else if (PriceUtil.between(percent.floatValue(), (float) 1, (float) 3.5,
						(float) 0) && n == 0) {
					n++;
				} else if (PriceUtil.between(percent.floatValue(), (float) 1, (float) 3.5,
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
			preStock.put(stockId,date);
			List info = StockPriceWareHouse.getStockInfoList(stockId,new ArrayList(){{
				add(date);
			}});
			Map m = (Map)info.get(0);
			BigDecimal ma5 = (BigDecimal) m.get("MA5");
			BigDecimal ma10 = (BigDecimal)m.get("MA10");
			try {
				signal_day_insert(stockId, date, date, 0, 0.0, "", 0, ma5.doubleValue(), ma10.doubleValue(), 1, 0, "", 0.0);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return false;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.stock.model.StockModel#getBackDayCount()
	 */
	@Override
	public int getBackDayCount() {
		// TODO Auto-generated method stub
		return N_DAYS+10;
	}

	public int getHoldPeriod() {
		return holdPeriod;
	}

	public void setHoldPeriod(int holdPeriod) {
		this.holdPeriod = holdPeriod;
	}
}
