package com.stock.model.tb;

/*
 * �����ࣺ��ʼ���ڣ��������ڡ��͵㡢�ߵ㡢�Ƿ����������ơ�����MACD����
 * 
 */
public class Trend {
	public Trend() {
		super();
	}
	@Override
	public String toString() {
		return "Trend [beginDate=" + beginDate + ", endDate=" + endDate
				+ ", lowPrice=" + lowPrice + ", HighPrice=" + HighPrice
				+ ", isRise=" + isRise + ", macdVol=" + macdVol + "]";
	}
	public Trend(String beginDate, String endDate, double lowPrice, double highPrice, Boolean isRise, double macdVol) {
		super();
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.lowPrice = lowPrice;
		this.HighPrice = highPrice;
		this.isRise = isRise;
		this.macdVol = macdVol;
	}
	String beginDate;
	public String getBeginDate() {
		return beginDate;
	}
	public void setBeginDate(String beginDate) {
		this.beginDate = beginDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public double getLowPrice() {
		return lowPrice;
	}
	public void setLowPrice(double lowPrice) {
		this.lowPrice = lowPrice;
	}
	public double getHighPrice() {
		return HighPrice;
	}
	public void setHighPrice(double highPrice) {
		HighPrice = highPrice;
	}
	public Boolean getIsRise() {
		return isRise;
	}
	public void setIsRise(Boolean isRise) {
		this.isRise = isRise;
	}
	public double getMacdVol() {
		return macdVol;
	}
	public void setMacdVol(double macdVol) {
		this.macdVol = macdVol;
	}
	String endDate;
	double lowPrice;
	double HighPrice;
	Boolean isRise;
	double macdVol;
}
