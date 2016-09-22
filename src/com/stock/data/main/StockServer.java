package com.stock.data.main;

import com.stock.data.stockcode.StockCodeDbUtil;
import com.stock.data.stockcode.StockCodeUtil;
import com.stock.data.stockprice.StockPriceUtil;

import java.util.ArrayList;
import java.util.List;

public class StockServer {
	
	public static void main(String[] args) throws Exception {

		String flag="index";

		//获取最新的全量股票代码
		List<String> stockCodeList = StockCodeUtil.getStockListFromEastMoney();
		List stockCodes = new ArrayList(2000);
		for(String code:stockCodeList){
			System.out.println(code);
			if(code!=null&&!"".equals(code)){
				stockCodes.add(code);
			} 
		}
		List indexCodes = new ArrayList();
		indexCodes.add("sh000001");//上证
		indexCodes.add("sz399001");//深圳
		indexCodes.add("sh000300");//沪深300
		indexCodes.add("sz399006");//创业板
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
		} else if(flag.equals("daily")){
			StockPriceUtil.importDailyInfo(stockCodeList);
		} else if(flag.equals("index")){
			StockPriceUtil.clearIndex();
			StockPriceUtil.importIndex(indexCodes);
			StockPriceUtil.mergeIndex();
		}
		
				
	}

	

}
