/**
 * 
 */
package com.stock.util;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.stock.data.stockprice.StockPriceWareHouse;

import util.BeanFactory;

/**
 * @author hahazhu
 * 
 */
public class PriceUtil {
	private static final JdbcTemplate jdbc  = (JdbcTemplate) BeanFactory.getInstance().getBean("jdbcTemplate");

	/**
	 * @param date1
	 * @param stock
	 * @return
	 */
	public static float getNextDayPrice(String date1, String stock) {
		// TODO Auto-generated method stub
		String datenext = DateUtil.getNextTradeDay(date1, stock);
		return StockPriceWareHouse.getStockPriceByDay(stock,datenext);
	}
	public static boolean between(float price,float last_price,float up_percent,float down_percent){
		boolean rst=false;
		if((price/last_price>=down_percent)&&(price/last_price<=up_percent)){
			return true;
		}
		return rst;
	}
	public static int getSucTime(List array) {
		int rst = 0;
		for (int i = 0; i < array.size(); i++) {
			double pro = Double.parseDouble(((List) array.get(i)).get(2)
					.toString());
			if (pro > 0) {
				rst++;
			}
		}
		return rst;
	}

	public static int getFailTime(List array) {
		int rst = 0;
		for (int i = 0; i < array.size(); i++) {
			double pro = Double.parseDouble(((List) array.get(i)).get(2)
					.toString());
			if (pro < 0) {
				rst++;
			}
		}
		return rst;
	}

	public static int getMaxSuc(List array) {
		double profit = 0;
		int rst = 0;
		for (int i = 0; i < array.size(); i++) {
			double pro = Double.parseDouble(((List) array.get(i)).get(2)
					.toString());
			if (profit < pro) {
				rst = i;
				profit = pro;
			}
		}
		return rst;
	}

	public static int getMaxFail(List array) {
		double profit = 0;
		int rst = 0;
		for (int i = 0; i < array.size(); i++) {
			double pro = Double.parseDouble(((List) array.get(i)).get(2)
					.toString());
			if (profit > pro) {
				rst = i;
				profit = pro;
			}
		}
		return rst;
	}

	public static double getMean(List array) {
		double mean = 0;
		for (int i = 0; i < array.size(); i++) {
			double pro = Double.parseDouble(((List) array.get(i)).get(2)
					.toString());
			mean += pro;
		}
		return mean / array.size();
	}

}
