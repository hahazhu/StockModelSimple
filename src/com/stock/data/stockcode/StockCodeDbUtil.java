package com.stock.data.stockcode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

public class StockCodeDbUtil {
	private static Logger logger = Logger.getLogger("AllStockJob");
	private static JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance()
			.getBean("jdbcTemplate");

	public static void refreshStockCodeInDB(List stockCode) throws SQLException {
		// 对股票代码拆分为 代码+市场
		List params = new ArrayList();
		for (Iterator it = stockCode.iterator(); it.hasNext();) {
			String tmp = (String) it.next();
			List param = new ArrayList();
			param.add(0, tmp.substring(2));
			param.add(1, tmp.substring(0, 2));
			params.add(param.toArray());
		}
		jdbc.update("delete from stock_info_tmp ");
		jdbc.batchUpdate(
				"insert into stock_info_tmp(stock_id,market) values(?,?)",
				params);
		logger.info("股票代码入库");

	}

	public static List getNewStock() {
		List result = null;
		result = jdbc.queryForList("select stock_id,market from stock_info_tmp a "
				+ "where (stock_id) not in (select stock_id from stock_info)"
						);
		return result;

	}
}
