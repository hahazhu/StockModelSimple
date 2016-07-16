package com.stock.data.stockprice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

import com.stock.job.ThirtyMinJob;
import com.stock.job.XueqiuStockPriceJob;

public class StockPriceUtil {
	private static Logger logger = Logger.getLogger("StockPriceUtil");
	private static final int POLLSIZE=30;
//	static String sql = "insert into stock_day_tmp(stock_id,market,volume,open,high,close,low,chg,percent,turnrate,ma5,ma10,ma20,ma30,ema12,ema26,dif,dea,macd,d_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static void clear(){
		JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean("jdbcTemplate");
		jdbc.update("truncate table stock_day");
		logger.info(" table truncated!");
	}
	public static void clearThirty(){
		JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean("jdbcTemplate");
		jdbc.update("truncate table stock_thirty");
		logger.info(" stock_thirty table truncated!");
	}
	
	public static void importHisFromListAfterIndex(int first,List stockList)
			throws Exception {
		first = first < 0 ? 0 : first;
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(POLLSIZE);
		for (int i = first; i < stockList.size(); i++) {
			if(!"".equals((String) stockList.get(i))&&stockList.get(i)!=null){
				fixedThreadPool.execute(new XueqiuStockPriceJob((String) stockList.get(i))); 
			}
		}
		fixedThreadPool.shutdown();
	}
	
	public static void importThirtyHisFromListAfterIndex(int first,List stockList)
			throws Exception {
		first = first < 0 ? 0 : first;
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(POLLSIZE);
		for (int i = first; i < stockList.size(); i++) {
			if(!"".equals((String) stockList.get(i))&&stockList.get(i)!=null){
				fixedThreadPool.execute(new ThirtyMinJob((String) stockList.get(i))); 
			}
		}
		fixedThreadPool.shutdown();
	}
	
	
	public static void importHisFromListAfterCode(String code,List stockList)
			throws Exception {
		importHisFromListAfterIndex(getIndexByCode(code,stockList),stockList);
	}
	private static int getIndexByCode(String stock,List stockList){
		for(int i=0;i<stockList.size();i++){
			if(((String) stockList.get(i)).contains(stock))
				return i+1;
		}
		return 0;
	}
	
	//每日增量的
	
	
	public static List getTodayStockInfo(int first,List stockList)
			throws Exception {
		List stockInfo = new ArrayList();
		first = first < 0 ? 0 : first;
		for (int i = first; i < stockList.size(); i++) {
			String code = (String)stockList.get(i);
			if(code!=null&&!"".equals(code)){
				stockInfo.add(getStockInfoByCode(code).toArray());
			}
		}
		return stockInfo;
	}
	private static List getStockInfoByCode(String stockCode)
			throws IOException {
		// 仅仅打印
		List stockInfo =new ArrayList();
		stockInfo.add(stockCode.substring(2));
		stockInfo.add(stockCode.substring(0, 2));
//		stockInfo.add(DataUtil.GetNowDate());
		for(int j=0;j<10;j++){
			try{
				URL url = new URL("http://hq.sinajs.cn/?list=" + stockCode);
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(5000);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						connection.getInputStream(),"GBK"));
				String line = null;
				StringBuffer sb = new StringBuffer();
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				if (sb.length() > 0) {
					String rs = sb.toString();
					rs = rs.substring(rs.indexOf("\"") + 1, rs.lastIndexOf("\""));
					String[] rss = rs.split(",");

					for (int i = 0; i < rss.length; i++) {
						if(i==1||i==3||i==4||i==5||i==8||i==30){
							stockInfo.add(rss[i]);
						}
					}
					logger.info(stockCode+" dailyInfo done!");
					break;
				}
			}catch(Exception e){
				
			}
		}
		return stockInfo;
	}
}
