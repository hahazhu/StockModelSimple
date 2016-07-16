package com.stock.model.tb;
/*
 * 峰值类
 * 定义：三个K线如果存在如下情况则为峰值
 * 1、中间的高点高于前一天和后一天的高点，则这是一个高点Peak，isHigh=true
 * 2、中间的低点低于前一天和后一天的低点，则这是一个低点Peak，isHigh=false
 */
public class Peak {
	@Override
	public String toString() {
		return "Peak [date=" + date + ", price=" + price + ", isHigh=" + isHigh
				+ "]";
	}
	public String date;
	public double price;
	public Boolean isHigh;
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public Boolean getIsHigh() {
		return isHigh;
	}
	public void setIsHigh(Boolean isHigh) {
		this.isHigh = isHigh;
	}
	public Peak(String date, double price, Boolean isHigh) {
		super();
		this.date = date;
		this.price = price;
		this.isHigh = isHigh;
	}
	public Peak() {
		super();
	}
}
