package com.stock.data.main;

import java.util.ArrayList;
import java.util.List;

import com.stock.data.stockcode.StockCodeDbUtil;
import com.stock.data.stockcode.StockCodeUtil;
import com.stock.data.stockprice.StockPriceDbUtil;
import com.stock.data.stockprice.StockPriceUtil;

public class StockServer {
	
	public static void main(String[] args) throws Exception {
		
		String flag="daily";
		
		//获取最新的全量股票代码
		List<String> stockCodeList = StockCodeUtil.getStockListFromEastMoney();
		List stockCodes = new ArrayList(2000);
		for(String code:stockCodeList){
			System.out.println(code);
			if(code!=null&&!"".equals(code)){
				stockCodes.add(code);
			} 
		}
		//入库
		StockCodeDbUtil.refreshStockCodeInDB(stockCodes);
		
		if(flag.equals("history")){
			//导入存量
			StockPriceUtil.clear();
			StockPriceUtil.importHisFromListAfterIndex(0,stockCodeList );
		} else if(flag.equals("thirty")){
			//导入存量
			StockPriceUtil.clearThirty();
			StockPriceUtil.importThirtyHisFromListAfterIndex(0,stockCodeList );
		}
		else if(flag.equals("daily")){
			//每日增量
			List stockPrices=StockPriceUtil.getTodayStockInfo(0, stockCodes);
			//导入当日价格
			StockPriceDbUtil.importDailyPrice(stockPrices);
			//计算macd等指标
			String date10 = ((Object[])stockPrices.get(0))[7].toString();
			StockPriceDbUtil.calDailyPrice(stockCodes,date10.substring(0, 4)+date10.substring(5, 7)+date10.substring(8, 10));
			
		}
		
				
	}

	

}
