/**
 * 
 */
package com.stock.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.list.TreeList;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

/**
 * @author hahazhu
 *
 */
public class DateUtil {

	/**
	 * @param stock
	 * @param dateBegin
	 * @param dateEnd
	 * @return
	 */
	private static final JdbcTemplate jdbc  = (JdbcTemplate) BeanFactory.getInstance().getBean("jdbcTemplate");
	private static final Map dateMap = new ConcurrentHashMap();
	private static final Map thirtyMap = new ConcurrentHashMap();

	private static final Logger logger = Logger.getLogger("BacktestingFrame");
	
	public static void main(String[] args){
		initDateMap(10);
		System.out.println(getRange("600590","20151031","20160301"));
		System.out.println(getNextTradeDay("20160301","001979"));
		System.out.println(getBackdate("20151231",3,"600590"));
	}
	
	/**
	 * @param stock
	 * @param dateBegin
	 * @param dateEnd
	 * @return 返回stock上从dateBegin到dateEnd的日期，顺序排列，前后都包含
	 */
	@SuppressWarnings("rawtypes")
	public static List<String> getRange(String stock, String dateBegin,
			String dateEnd) {
		try{
		List l = (List) dateMap.get(stock);
		int beg = l.lastIndexOf(dateBegin);
		int end = l.lastIndexOf(dateEnd)+1;
		while(beg<0){
			String nextDay = getNextDay(dateBegin);
			if(nextDay==null)
				break;
			if(dateEnd.equals(nextDay))
				break;
			beg = l.lastIndexOf(nextDay);
			dateBegin=nextDay;
		}
		while(end<=0){
			String lastDay = getLastDay(dateEnd);
			if(lastDay==null)
				break;
			if(dateBegin.equals(lastDay))
				break;
			end = l.lastIndexOf(lastDay);
			dateEnd = lastDay;
			
		}
		if(beg>=0&&end<=l.size()&&end>0){
			return l.subList(beg,end );
		} else {
			logger.debug(stock+" no data");
			return null;
		}
		}catch(Exception e){
			e.printStackTrace();
			System.err.println(dateBegin+'\t'+dateEnd);
			return null;
		}
	}
	/**
	 * @param stock
	 * @param dateBegin yyyy-mm-dd
	 * @param dateEnd yyyy-mm-dd
	 * @return 返回stock上从dateBegin到dateEnd的日期，顺序排列，前后都包含
	 */
	@SuppressWarnings("rawtypes")
	public static List<String> get30KRange(String stock, String dateBegin,
										String dateEnd) {
//		logger.info(dateBegin+" "+dateEnd);
		try{
			List l = (List) thirtyMap.get(stock);
			int beg = -1,end = -1;
			for (int i = 0; i < l.size(); i++) {
				String s = (String) l.get(i);
				if(s.startsWith(dateBegin)){
					beg=i;
					break;
				}
			}
			for (int i = 0; i < l.size(); i++) {
				String s = (String) l.get(i);
				if(s.startsWith(dateEnd)){
					end=i;
				}
			}
			if(beg==-1||end==-1){
//				logger.info(dateBegin+":"+dateEnd + " " +stock);
				return new ArrayList<String>();
			}
//			logger.info(l.subList(beg,end+1));
			return l.subList(beg,end+1);
		}catch(Exception e){
			logger.error(e);
			e.printStackTrace();
			System.err.println(dateBegin+'\t'+dateEnd);
			return null;
		}
	}

	public static String getNextDay(String date) {
		if(date!=null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date date2 = null;
			try {
				date2 = sdf.parse(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// 定义Date 

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date2);
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			return sdf.format(calendar.getTime());
		} else {
			return null;
		}
	}
	public static String getLastDay(String date) {
		if(date==null)
			return null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date date2 = null;
		try {
			date2 = sdf.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// 定义Date 
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date2);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		return sdf.format(calendar.getTime());
	}
	/**
	 * @param date
	 * @param stock
	 * @return 返回这个股票下一个交易日的日期
	 */
	public static String getNextTradeDay(String date, String stock) {
		List l = (List) dateMap.get(stock);
		final int pos = l.lastIndexOf(date);
		if(l!=null&&l.size()!=0&&pos<l.size()-1){
			return (String) l.get(pos+1);
		} else {
			return null;
		}
	}
	public static String getLastTradeDay(String date, String stock) {
		List l = (List) dateMap.get(stock);
		if(l==null)
			return null;
		int pos = l.lastIndexOf(date);
		if(pos>0){
			return (String) l.get(pos-1);
		}
		if(pos==0){
			return null;
		}
		while(pos<0){
			date = getLastDay(date);
			if(Params.dateBegin.equals(date))
				return null;
			pos = l.lastIndexOf(date);
		}
		return date; 
	}


	public static void clear(){
		dateMap.clear();
	}

	public static void initDateMap(int i){
		logger.info("initial date");
		List<Map<String, Object>> list = jdbc.queryForList("select stock_id,d_date from stock_day "
				+ "where  d_date between date_sub(?,interval ? day) and ? order by stock_id,d_date ", new Object[]{
						Params.dateBegin,i,Params.dateEnd
				});
		Iterator it = list.iterator();
		while(it.hasNext()){
			Map m = (Map)it.next();
			String id = (String) m.get("stock_id"); 
			List l = null;
			if(dateMap.containsKey(id)){
				l = (List) dateMap.get(id);
				l.add(m.get("d_date"));
			} else {
				l = new TreeList();
				l.add(m.get("d_date"));
			}
			dateMap.put(id, l);
		}
		logger.info("date done");
		
	}
	public static void init30timeMap(int i){
		logger.info("initial 30k");
		List<Map<String, Object>> list = jdbc.queryForList("select stock_id,t_time,date_format(t_time,'%Y%m%d') d_date from stock_thirty "
				+ "where  date_format(t_time,'%Y%m%d') between date_sub(?,interval ? day) and ? order by stock_id,t_time ", new Object[]{
				Params.dateBegin,i,Params.dateEnd
		});
		Iterator it = list.iterator();
		while(it.hasNext()){
			Map m = (Map)it.next();
			String id = (String) m.get("stock_id");
			List l;
			if(thirtyMap.containsKey(id)){
				l = (List) thirtyMap.get(id);
				l.add(m.get("t_time"));
			} else {
				l = new TreeList();
				l.add(m.get("t_time"));
			}
			thirtyMap.put(id, l);
		}
		logger.info("30k done");
	}
	/**
	 * @param date
	 * @param i
	 * @param stock
	 * @return 该股票date往前追溯i-1天的日期。 20010104，i=3时，返回20010102 
	 */
	public static String getBackdate(String date, int i, String stock) {
		String backdate=date;
		int j=0;
		while(j<i){
			backdate = getLastTradeDay(backdate, stock);
			if(backdate==null)
				break;
			j++;
		}
		return backdate;
	}

}
