/**
 * 
 */
package com.stock.util;

/**
 * @author hahazhu
 *
 */
public class Params {
	public static String dateBegin = new String("20160201");
	public static int capital = 200000;

	public static final int THE_COUNT = 1000;
	public static final int THE_PERCENT = 20;
	public static String dateEnd = new String("20160601");
	public static float profitPer = (float) 1.1;
	public static float stopLossPer = (float) 0.9;
	public static float withdrawPercent = (float)0.05;
	public static int holdPeriod = 9;
	
	/*
	 * 三买参数
	 * 
	 */
	//-----买点参数
	public static int TB_N_DAYS = 120;  //回溯天数
	public static int TB_N_VALID_DAYS = 80;//回溯天数中最少非停牌天数
	public static double TB_BACK_PERCENTS = 0.89;// 计算买点时候的：当没有30分钟线时，用回撤百分比代替
	public static int TB_ZS_OR_MAX = 1;  //买点回跌是判断 1、不能跌破最高点 2、不能跌破zs高点
	public static boolean TB_FIRST_TB = true;  //判断是否是第一个三买，即上上个中枢的突破趋势是向下的，上个中枢的突破趋势是向上的
	public static double TB_TREND_RISE_PERCENTS = 1.3;// 这次突破趋势的涨幅，从最低点到最高点涨幅不能超过该值(必须大于1，小于1则无效）
	//-----卖点参数
	public static int TB_SELL_ZS_OR_MAX = 1;  //卖点回跌是判断 1、不能跌破最高点 2、不能跌破zs高点
	public static double TB_SELL_PERCENTS = 0.98;// 计算卖点的时候：当下跌跌过zs的最高点的SELL_PERCENTS
	public static boolean TB_SELL_BEILI = true;// 是否出现背离的时候要卖出
	public static boolean TB_SELL_INVALID_SELL= true;  //数据太少的时候是不是卖出
}
