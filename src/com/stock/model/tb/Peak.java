package com.stock.model.tb;
/*
 * ��ֵ��
 * ���壺����K������������������Ϊ��ֵ
 * 1���м�ĸߵ����ǰһ��ͺ�һ��ĸߵ㣬������һ���ߵ�Peak��isHigh=true
 * 2���м�ĵ͵����ǰһ��ͺ�һ��ĵ͵㣬������һ���͵�Peak��isHigh=false
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
