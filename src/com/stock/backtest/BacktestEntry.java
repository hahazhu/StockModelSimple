/**
 * 
 */
package com.stock.backtest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

import com.stock.data.stockprice.StockPriceWareHouse;
import com.stock.model.StockModel;
import com.stock.model.TrendBuyV4Model;
import com.stock.util.DateUtil;
import com.stock.util.Params;

/**
 * @author hahazhu
 *
 */
public class BacktestEntry {
	private static Logger logger = Logger.getLogger("BacktestingFrame");
	private static JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean("jdbcTemplate");
	

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		List backTestParam = BacktestResult.testResult;
		
		for(int year =2016;year<2017;year++){
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);
			Params.dateBegin = year+"0101";
			int yearEnd = year+1;
			Params.dateEnd = yearEnd+"0101";

			StockModel tb = new TrendBuyV4Model();

			DateUtil.initDateMap(tb.getBackDayCount());
			StockPriceWareHouse.setBackCount(tb.getBackDayCount());
			StockPriceWareHouse.initPriceMap();
			for(int j=10;j<11;j++){
//				tb = new TrendBuyV4Model();
				Params.holdPeriod = j;
				List stockList = jdbc.queryForList("select distinct stock_id from stock_day where  d_date > ? ",
						new Object[]{ Params.dateBegin },String.class);
				String testTime = jdbc.queryForObject("select date_format(sysdate(),'%Y-%m-%d %H:%i:%S') from dual", String.class);
				BacktestingFrame bt = new BacktestingFrame(Params.dateBegin,Params.dateEnd,stockList,Params.capital,tb, testTime); 
				fixedThreadPool.execute(bt);
				
			}
			fixedThreadPool.shutdown();
			fixedThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			StockPriceWareHouse.clearMemory();
			DateUtil.clear();
		}
		for(Iterator it =backTestParam.iterator();it.hasNext();){
			Map tmp = (Map) it.next();
			printStats(tmp);
		}
		logger.info("--------------------简报--------------------");
		for(Iterator it =backTestParam.iterator();it.hasNext();){
			Map tmp = (Map) it.next();
			logger.info(tmp.get("start_date")+"-"+tmp.get("end_date")+"\t"+
					"return="+tmp.get("return")+"\t"+
					"holdPeriod="+tmp.get("holdPeriod")+"\t"+
					"wins="+tmp.get("wins")+" lose="+tmp.get("lose")+"\t"+
					"winPercent="+tmp.get("winPercent"));
		}
	}
	private static void printStats(Map map) {
		String modelName = (String) map.get("modelName");
		String batch = (String) map.get("batch");
		logger.info("-------------统计信息--------------");
		logger.info("-------------收益最高操作top10--------------");
		List topProfits = jdbc.queryForList(
				"select a.profit,b.n_buy_amount,b.c_stock_id,b.d_buy_date buy_days,b.d_sell_date "
				+ "from "
				+ "(SELECT     avg(a.d_sell_price) / avg(a.d_buy_price) profit,a.modelname , a.d_test_batch , a.c_stock_id , a.d_buy_date "
				+ "FROM    dat_model_buy a "
				+ "where  a.modelname=? and a.d_test_batch=? and a.d_sell_date is not null "
				+ "group by a.modelname , a.d_test_batch , a.c_stock_id , a.d_buy_date "
				+ "order by profit desc limit 10) a ,dat_model_buy b "
				+ "where a.modelname = b.modelname and a.d_test_batch = b.d_test_batch "
				+ "and a.c_stock_id = b.c_stock_id and a.d_buy_date = b.d_buy_date group by b.c_stock_id , b.d_buy_date order by a.profit desc"
				,
				new Object[]{ modelName,batch });
		for(Iterator it=topProfits.iterator();it.hasNext();){
			Map m = (Map) it.next();
			logger.info(m.get("c_stock_id") +"\t"+
					m.get("n_buy_amount")+"\t"+m.get("profit")+"\t买入日期:"+m.get("buy_days")+"\t卖出日期"+m.get("d_sell_date"));
		}
		logger.info("-------------收益最低操作top10--------------");
		List toploss = jdbc.queryForList(
				"select a.profit,b.n_buy_amount,b.c_stock_id,b.d_buy_date buy_days,b.d_sell_date "
				+ "from "
				+ "(SELECT     avg(a.d_sell_price) / avg(a.d_buy_price) profit,a.modelname , a.d_test_batch , a.c_stock_id , a.d_buy_date "
				+ "FROM    dat_model_buy a where  a.modelname=? and a.d_test_batch=? and a.d_sell_date is not null "
				+ "group by a.modelname , a.d_test_batch , a.c_stock_id , a.d_buy_date "
				+ "order by profit limit 10) a ,dat_model_buy b "
				+ "where a.modelname = b.modelname and a.d_test_batch = b.d_test_batch "
				+ "and a.c_stock_id = b.c_stock_id and a.d_buy_date = b.d_buy_date group by b.c_stock_id , b.d_sell_date order by a.profit "
				,
				new Object[]{ modelName,batch });
		for(Iterator it=toploss.iterator();it.hasNext();){
			Map m = (Map) it.next();
			logger.info(m.get("c_stock_id") +"\t"+
					m.get("n_buy_amount")+"\t"+m.get("profit")+"\t买入日期:"+m.get("buy_days")+"\t卖出日期"+m.get("d_sell_date"));
		}
		logger.info("-------------出现买入信号次数top10--------------");
		List topDays = jdbc.queryForList("SELECT  count(1) cnt,d_buy_date FROM dat_model_buy a"
				+ " where modelname=? and d_test_batch=?"
				+ "  group by a.modelname , a.d_test_batch , a.d_buy_date order by cnt desc limit 10 ",
				new Object[]{ modelName,batch });
		for(Iterator it=topDays.iterator();it.hasNext();){
			Map m = (Map) it.next();
			logger.info("买入股票数"+m.get("cnt")+"\t 买入日期"+m.get("d_buy_date"));
		}
	}

}
