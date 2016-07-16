/**
 * 
 */
package com.stock.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author hahazhu
 *
 */
public class Stock {
 
	private String stockId;
	private List<Map> history = new LinkedList();
	private int totalNum = 0;
	private float costPrice;
	public float getCostPrice(){
		return costPrice;
	}
	
	public int getTotalNum(){
		return totalNum;
	}
	public Stock(String stock){
		this.stockId = stock;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stockId == null) ? 0 : stockId.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stock other = (Stock) obj;
		if (stockId == null) {
			if (other.stockId != null)
				return false;
		} else if (!stockId.equals(other.stockId))
			return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Stock [stockId=" + stockId + ", history=" + history + "]";
	}

	public void addCount(final int count,final float price,String date){
		if(totalNum==0){
			costPrice=price;
		} else {
			costPrice = (totalNum*costPrice+count*price)/(totalNum+count);
		}
		Map m = new HashMap();
		m.put(date, new HashMap(){{
			put(count,price);
		}});
		history.add(m);
		totalNum+=count;
	}
	public void minusCount(final int count,final float price,String date){
		if(totalNum>count){
			costPrice = (totalNum*costPrice-count*price)/(totalNum-count);
		} else {
			costPrice=0;
		}
		Map m = new HashMap();
		m.put(date, new HashMap(){{
			put(-count,price);
		}});
		history.add(m);
		totalNum-=count;
	}

}
