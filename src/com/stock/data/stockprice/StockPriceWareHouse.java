/**
 * 
 */
package com.stock.data.stockprice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

import com.stock.util.Params;

/**
 * @author hahazhu
 *
 */
public class StockPriceWareHouse {
	private static JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean(
			"jdbcTemplate");
	private static Map allStockPrice = new ConcurrentHashMap();

	private static Logger logger = Logger.getLogger("StockPriceWareHouse");
	private StockPriceWareHouse swh = new StockPriceWareHouse() ;  

	private static int backCount = 0;
	
	public static void setBackCount(int i){
		backCount = i;
	}
	
	private StockPriceWareHouse(){
		
	}
	public StockPriceWareHouse getInstance(){
		return swh;
	}
	public static void clearMemory(){
		allStockPrice.clear();
	}
	
	public static void main(String[] args){
		backCount = 10;
		getStockPriceMap("600019");
	}
	
	public static void removeStock(String stock){
		allStockPrice.remove(stock);
	}
	/**
	 * @param stock
	 * @return 返回这个股票从回测开始日期到现在的所有交易数据,map的key为日期，value为存储所有信息的map
	 */
	public static Map getStockPriceMap(String stock){
		if(!allStockPrice.containsKey(stock)){
			initPriceMap();
			logger.info("com.stock price done!");
		} 
		return (Map) allStockPrice.get(stock);
	}
	public static void initPriceMap(){
		List<Map<String, Object>> prices = jdbc.queryForList("select stock_id,open,high,low,ma5,ma10,macd,d_date,"
					+ "close,percent,volume from stock_day where "
					+ "d_date between date_sub(?,interval ? day) and ?  order by stock_id,d_date",new Object[]{
				Params.dateBegin,backCount,Params.dateEnd
			});
		for(Iterator it = prices.iterator();it.hasNext();){
			Map m = (Map) it.next();
			String id = (String) m.get("stock_id");
			Map tmp =  (allStockPrice.containsKey(id)?(Map)allStockPrice.get(id):new HashMap());
			tmp.put(m.get("d_date"), m);
			allStockPrice.put(id, tmp);
		}
	}
	public static List getStockInfoList(String stock,List dateList){
		Map stockPriceMap = getStockPriceMap(stock);
		List result = new LinkedList();
		if(dateList==null)
			return result;
		for(Iterator it = dateList.iterator();it.hasNext();){
			String date = (String) it.next();
			Map tmp = (Map) stockPriceMap.get(date);
			result.add(tmp);
		}
		return result;
		
	}
	public static List getStockPriceList(String stock,List dateList){
		Map stockPriceMap = getStockPriceMap(stock);
		List result = new LinkedList();
		for(Iterator it = dateList.iterator();it.hasNext();){
			String date = (String) it.next();
			Map tmp = (Map) stockPriceMap.get(date);
			result.add(tmp.get("open"));
		}
		return result;
		
	}
	/**
	 * @param stock
	 * @param datenext
	 */
	public static float getStockPriceByDay(String stock, String datenext) {
		// TODO Auto-generated method stub
		Map m = getStockPriceMap(stock);
		Map values = (Map) m.get(datenext);
		BigDecimal open = (BigDecimal) values.get("open");
		return open.floatValue();
		
	}

}
