/**
 * 
 */
package com.stock.job;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.stock.data.stockcode.StockCodeDbUtil;
import com.stock.data.stockcode.StockCodeUtil;
import com.stock.data.stockprice.StockPriceDbUtil;
import com.stock.data.stockprice.StockPriceUtil;

/**
 * @author hahazhu
 * 
 */
public class DailyStockJob {
	Logger logger = Logger.getLogger(this.getClass().getName());
	public void execute() {
		List<String> stockCodeList;
		logger.info("daily stock start!!!!!");
		try {
			stockCodeList = StockCodeUtil.getStockListFromSina();

			List stockCodes = new ArrayList(2000);
			for (String code : stockCodeList) {
				if (code != null && !"".equals(code)) {
					stockCodes.add(code);
					logger.info(code);
				}
			}
			// 入库
			StockCodeDbUtil.refreshStockCodeInDB(stockCodes);

			// 每日增量
			List stockPrices = StockPriceUtil.getTodayStockInfo(0, stockCodes);
			// 导入当日价格
			StockPriceDbUtil.importDailyPrice(stockPrices);
			// 计算macd等指标
			String date10 = ((Object[])stockPrices.get(0))[7].toString();
			StockPriceDbUtil.calDailyPrice(stockCodes,date10.substring(0, 4)+date10.substring(5, 7)+date10.substring(8, 10));
		} catch (IOException e) {
			logger.error(e.toString());
		} catch (SQLException e) {
			logger.error(e.toString());
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}

}
