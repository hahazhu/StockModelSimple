package com.stock.data.main;

import com.stock.data.stockcode.StockCodeDbUtil;
import com.stock.data.stockcode.StockCodeUtil;
import com.stock.data.stockprice.StockPriceUtil;

import java.util.ArrayList;
import java.util.List;

public class StockServer {
	
	public static void main(String[] args) throws Exception {

		String flag="history";

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
			StockPriceUtil.mergeHis();
		} else if(flag.equals("thirty")){
			//导入存量
			StockPriceUtil.clearThirty();
			StockPriceUtil.importThirtyHisFromListAfterIndex(0, stockCodeList);
			StockPriceUtil.mergeThirty();
		}
		else if(flag.equals("daily")){
			StockPriceUtil.importDailyInfo(stockCodeList);
		}
		
				
	}

	

}
