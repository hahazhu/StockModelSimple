/**
 * 
 */
package com.stock.backtest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.stock.data.stockprice.StockPriceWareHouse;
import com.stock.model.Stock;
import com.stock.util.DateUtil;

/**
 * @author hahazhu
 *
 */
public class Inventory {

	private float cash;
	private Map<String,Stock> stocks = new HashMap();

	/**
	 * @param cash
	 */
	public Inventory(int cash) {
		// TODO Auto-generated constructor stub
		this.cash = cash;
	}

	/**
	 * @param stock
	 * @return
	 */
	public boolean hasStock(String stockid) {
		// TODO Auto-generated method stub
		if(stocks.containsKey(stockid)){
			Stock stock = stocks.get(stockid);
			if(stock.getTotalNum()!=0)
				return true;
		}
		return false;
	}
	public boolean addStock(String stockId,int count,float price,String date){
		Stock stock = null;
		if(cash-count*price>0){
			if(stocks.containsKey(stockId)){
				stock = (Stock) stocks.get(stockId);
				stock.addCount(count, price, date);
				this.cash = cash-count*price;
				return true;
			} else {
				stock = new Stock(stockId); 
				stock.addCount(count, price, date);
				this.cash = cash-count*price;
				stocks.put(stockId, stock);
				return true;
			}
		} else {
			return false;
		}
	}
	public boolean sellStock(String stockId,int count,float price,String date){
		if(stocks.containsKey(stockId)){
			Stock stock = (Stock) stocks.get(stockId);
			stock.minusCount(count, price, date);
			this.cash = cash+count*price;
			return true;
		} else {
			return false;
		}
	}
	public boolean sellOutStock(String stockId,float price,String date){
		Stock stock = null;
		if(stocks.containsKey(stockId)){
			stock = (Stock) stocks.get(stockId);
			int count = stock.getTotalNum();
			stock.minusCount(count, price, date);
			this.cash = cash+count*price;
			return true;
		} else {
			return false;
		}
	}
	public String toString(){
		StringBuffer sb = new StringBuffer();
		for(String stock:stocks.keySet()){
			if(hasStock(stock)){
				sb.append(stock+"\n");
			}
		}
		sb.append("cash="+cash);
		return sb.toString();
	}
	public float getCash(){
		return cash;
	}
	public float getTotalVal(final String date){
		float totalval = cash;
		for(final String stockId:stocks.keySet()){
			if(hasStock(stockId)){
				Stock stock = stocks.get(stockId);
				List result = StockPriceWareHouse.getStockInfoList(
						stockId,  new ArrayList(){{ add(date);}});
				Map m = (Map) result.get(0);
				if(m!=null){
					BigDecimal close = (BigDecimal) m.get("close");
					totalval+=close.floatValue()*stock.getTotalNum();
				} else {
					result = StockPriceWareHouse.getStockInfoList(
							stockId,  new ArrayList(){{ 
								add(DateUtil.getLastTradeDay(date, stockId));
							}});
					m = (Map) result.get(0);
					BigDecimal close = (BigDecimal) m.get("close");
					totalval+=close.floatValue()*stock.getTotalNum();
				}
			}
		}
		return totalval;
	}
	public String getAccountDetail(String date){
		StringBuffer sb = new StringBuffer();
		sb.append(date+" 市值：");
		sb.append(this.getTotalVal(date));
		sb.append("\n现金：");
		sb.append(this.getCash());
		sb.append("\n持仓股票：");
		for(String stock:stocks.keySet()){
			if(hasStock(stock)){
				sb.append(stock+"\t");
			}
		}
		return sb.toString();
	}
	public float getCostPrice(String stockId){
		Stock stock = stocks.get(stockId);
		return stock.getCostPrice();
	}
}
