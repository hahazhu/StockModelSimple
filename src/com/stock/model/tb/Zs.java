package com.stock.model.tb;
/*
 * 中枢类：开始时间、结束时间、最高点、最低点、次高点、次低点、中枢高点、中枢低点，中枢趋势、三个趋势中的第一个趋势、第二个趋势、第三个趋势。
 * sHighPrice,sLowPrice:次高点和次低点。对于三个趋势形成的小中枢来说，sHighPrice=zsHighPrice,sLowPrice= zsLowPrice
 * 但是对于大中枢来说不一定。所以这个field不可少
 * Zs趋势：记录形成这个中枢形成前的一个突破趋势，非常关键。
 * 
 * delTrend：趋势删除，由于循环计算三个趋势重叠->中枢出现->中枢扩展->三个趋势无重叠，形成突破趋势，计算后，突破趋势的时间、高低点都通过扩展放到了这个ZS实例中，故要删除
 * 
 * 删除的方法很简单：对每一个ZS循环所有的ZsTrend进行删除：调整时间（把突破趋势前或者后面的时间修改成更靠近zs的时间点）、调整最高点（使用第二高点）
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
