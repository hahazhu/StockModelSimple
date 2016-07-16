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
import com.stock.data.stockprice.StockPriceUtil;

/**
 * @author hahazhu
 * 
 */
public class AllStockJob {
	private AllStockJob(){
		
	}
	Logger logger = Logger.getLogger("AllStockJob");
	public void execute() {
		List<String> stockCodeList;
		logger.info(" all com.stock start!!!!");
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

			StockPriceUtil.clear();
			StockPriceUtil.importHisFromListAfterIndex(0,stockCodeList );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
