/**
 * 
 */
package com.stock.data.main;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

import com.stock.data.stockprice.StockPriceWareHouse;
import com.stock.model.StockModel;
import com.stock.model.TrendBuyV2Model;
import com.stock.util.DateUtil;
import com.stock.util.Params;

/**
 * @author hahazhu
 *
 */
public class DailySignal {

	/**
	 * @param args
	 */
	private static JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean("jdbcTemplate");
	private static Logger logger = Logger.getLogger("BacktestingFrame");

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String date = jdbc.queryForObject("select date_format(sysdate(),'%Y%m%d') from dual", String.class);
		date="20160408";
		List<String> stockList = jdbc.queryForList("select distinct stock_id from stock_day where  d_date = ?  ",
				new Object[]{ date },String.class);
		Params.dateBegin = date;
		Params.dateEnd = date;
		StockModel tb = new TrendBuyV2Model();
		DateUtil.initDateMap(tb.getBackDayCount());
		StockPriceWareHouse.setBackCount(tb.getBackDayCount());
		for(String stock_id:stockList){
			tb.setDate(date);
			tb.setStock(stock_id);
			if(tb.hasBuy()){
				logger.info(stock_id+"有买点");
			}
		}

	}

}
