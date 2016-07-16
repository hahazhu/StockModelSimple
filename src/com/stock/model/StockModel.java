/**
 * 
 */
package com.stock.model;

/**
 * @author hahazhu
 *
 */
public interface StockModel {

	/**
	 * @return
	 */
	boolean hasSell(float costPrice);

	/**
	 * @param date
	 */
	void setDate(String date);

	/**
	 * @param stock
	 */
	void setStock(String stock);

	/**
	 * @return
	 */
	boolean hasBuy();
	
	int getBackDayCount();
	float getBuyPrice();
	float getSellPrice();
	String getBuyDate();
	String getSellDate();
	int getHoldPeriod();
	void setHoldPeriod(int holdPeriod);

}
