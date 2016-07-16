package com.stock.model.tb;
/*
 * �����ࣺ��ʼʱ�䡢����ʱ�䡢��ߵ㡢��͵㡢�θߵ㡢�ε͵㡢����ߵ㡢����͵㣬�������ơ����������еĵ�һ�����ơ��ڶ������ơ����������ơ�
 * sHighPrice,sLowPrice:�θߵ�ʹε͵㡣�������������γɵ�С������˵��sHighPrice=zsHighPrice,sLowPrice= zsLowPrice
 * ���Ƕ��ڴ�������˵��һ�����������field������
 * Zs���ƣ���¼�γ���������γ�ǰ��һ��ͻ�����ƣ��ǳ��ؼ���
 * 
 * delTrend������ɾ��������ѭ���������������ص�->�������->������չ->�����������ص����γ�ͻ�����ƣ������ͻ�����Ƶ�ʱ�䡢�ߵ͵㶼ͨ����չ�ŵ������ZSʵ���У���Ҫɾ��
 * 
 * ɾ���ķ����ܼ򵥣���ÿһ��ZSѭ�����е�ZsTrend����ɾ��������ʱ�䣨��ͻ������ǰ���ߺ����ʱ���޸ĳɸ�����zs��ʱ��㣩��������ߵ㣨ʹ�õڶ��ߵ㣩
 */
public class Zs {

	@Override
	public String toString() {
		return "Zs [beginDate=" + beginDate + ", endDate=" + endDate
				+ ", lowPrice=" + lowPrice + ", sLowPrice=" + sLowPrice
				+ ", highPrice=" + highPrice + ", sHighPrice=" + sHighPrice
				+ ", zsLowPrice=" + zsLowPrice + ", zsHighPrice=" + zsHighPrice
				+ ", zsTrend=" + zsTrend + ", firstTrend=" + firstTrend
				+ ", secondTrend=" + secondTrend + ", thirdTrend=" + thirdTrend
				+ "]";
	}
	String beginDate;
	String endDate;
	double lowPrice;
	double sLowPrice;
	public double getsLowPrice() {
		return sLowPrice;
	}
	public void setsLowPrice(double sLowPrice) {
		this.sLowPrice = sLowPrice;
	}
	public double getsHighPrice() {
		return sHighPrice;
	}
	public void setsHighPrice(double sHighPrice) {
		this.sHighPrice = sHighPrice;
	}
	double highPrice;
	double sHighPrice;
	double zsLowPrice;
	double zsHighPrice;
	Trend zsTrend;
	Trend firstTrend;
	Trend secondTrend;
	Trend thirdTrend;
	public Trend getFirstTrend() {
		return firstTrend;
	}
	public void setFirstTrend(Trend firstTrend) {
		this.firstTrend = firstTrend;
	}
	public Trend getSecondTrend() {
		return secondTrend;
	}
	public void setSecondTrend(Trend secondTrend) {
		this.secondTrend = secondTrend;
	}
	public Trend getThirdTrend() {
		return thirdTrend;
	}
	public void setThirdTrend(Trend thirdTrend) {
		this.thirdTrend = thirdTrend;
	}

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
		return highPrice;
	}
	public void setHighPrice(double highPrice) {
		this.highPrice = highPrice;
	}
	public double getZsLowPrice() {
		return zsLowPrice;
	}
	public void setZsLowPrice(double zsLowPrice) {
		this.zsLowPrice = zsLowPrice;
	}
	public double getZsHighPrice() {
		return zsHighPrice;
	}
	public void setZsHighPrice(double zsHighPrice) {
		this.zsHighPrice = zsHighPrice;
	}
public Zs(String beginDate, String endDate, double lowPrice, double highPrice, double zsLowPrice,
			double zsHighPrice) {
		super();
		this.beginDate = beginDate;
		this.endDate = endDate;
		this.lowPrice = lowPrice;
		this.highPrice = highPrice;
		this.zsLowPrice = zsLowPrice;
		this.zsHighPrice = zsHighPrice;
	}
public Zs(){
	super();
}
public Trend getZsTrend() {
	return zsTrend;
}
public void setZsTrend(Trend zsTrend) {
	this.zsTrend = zsTrend;
}
public void delTrend(Trend zsTrend) {
	if(zsTrend ==null){
		return;
	}
	if (this.beginDate == zsTrend.beginDate){
		this.beginDate =zsTrend.endDate;
	}
	if (this.endDate == zsTrend.endDate){
		this.endDate =zsTrend.beginDate;
	}
	if(this.highPrice ==zsTrend.HighPrice){
		this.highPrice=this.sHighPrice;
	}
	if(this.lowPrice ==zsTrend.lowPrice){
		this.lowPrice=this.sLowPrice;
	}
}
}
