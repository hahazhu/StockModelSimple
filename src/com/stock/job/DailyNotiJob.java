/**
 * 
 */
package com.stock.job;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.stock.EmailNoti;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

import com.stock.data.stockprice.StockPriceDbUtil;
import com.stock.data.stockprice.StockPriceUtil;

/**
 * @author hahazhu
 * 
 */
public class DailyNotiJob {
	Logger logger = Logger.getLogger(this.getClass().getName());
	private static JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance()
			.getBean("jdbcTemplate");
	public void execute() {
		List<String> stockCodeList;
		logger.info("daily noti start!!!!!");
		try {

			List signal = jdbc.queryForList("select ma5_price,ma10_price,stock_id from stock_xsb_result where status='1'");
			Map signalMap = new HashMap();
			List stockCodes = new ArrayList(20);
			for(Iterator it = signal.iterator();it.hasNext();){
				Map m =(Map)it.next();
				BigDecimal ma5 = (BigDecimal) m.get("ma5_price");
				BigDecimal ma10 = (BigDecimal) m.get("ma10_price");
				float sig = (ma5.floatValue()+ma10.floatValue())/2;
				signalMap.put(m.get("stock_id"), sig);
				stockCodes.add(m.get("stock_id"));
			}
			// 每日增量
			List stockPrices = StockPriceUtil.getTodayStockInfo(0, stockCodes);
			
			for(Iterator it=stockPrices.iterator();it.hasNext();){
				Object[] stock = (Object[])it.next();
				logger.info(""+stock[0]+stock[1]);
				String low = (String)stock[5];
				float sig = (Float) signalMap.get(""+stock[1]+stock[0]);
				if(Float.valueOf(low)<sig){
					EmailNoti email = new EmailNoti();
					email.sendBuyNoti((String) stock[0]);
					logger.info(stock[0]+"最低价低于ma5与ma10的一半，出现信号!!!!!");
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.toString());
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.toString());
		}
	}

}
