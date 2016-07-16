/**
 * 
 */
package com.stock.backtest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

import com.stock.data.stockprice.StockPriceWareHouse;
import com.stock.model.StockModel;
import com.stock.model.ThirdBuyModel;
import com.stock.util.DateUtil;
import com.stock.util.Params;
import com.stock.util.PriceUtil;

/**
 * @author hahazhu
 *
 */
public class TbBacktestingFrame {
	Inventory inventory = null;
	private String dateBegin;
	private String dateEnd;
	private String market;
	private List<String> stockList;
	private static Logger logger = Logger.getLogger("BacktestingFrame");
	private static JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean("jdbcTemplate");
	public static int wins = 0;
	public static int lose = 0;
	String modelName;
	String testBatch;
	
	
	public TbBacktestingFrame(String dateBegin,String dateEnd,String market,int cash){
		this.dateBegin = dateBegin;
		this.dateEnd = dateEnd;
		this.market = market;
		inventory = new Inventory(cash);
	}
	public TbBacktestingFrame(String dateBegin,String dateEnd,List stockList,int cash,String modelName,String testBatch){
		this.dateBegin = dateBegin;
		this.dateEnd = dateEnd;
		this.stockList = stockList;
		this.inventory = new Inventory(cash);
		this.modelName = modelName;
		this.testBatch = testBatch;
	}
	public static void main(String[] args){
		Params.dateBegin = "20160101";
		Params.dateEnd = "20160522";
		List stockList = jdbc.queryForList("select distinct stock_id from stock_day where  d_date > ? ",
				new Object[]{ Params.dateBegin },String.class);
		String testTime = jdbc.queryForObject("select date_format(sysdate(),'%Y-%m-%d %H:%i:%S') from dual", String.class);
		ThirdBuyModel tb = new ThirdBuyModel();
		TbBacktestingFrame bt = new TbBacktestingFrame(Params.dateBegin,Params.dateEnd,stockList,200000,tb.getClass().getSimpleName(),testTime); 
		DateUtil.initDateMap(tb.getBackDayCount());
		StockPriceWareHouse.setBackCount(tb.getBackDayCount());
		bt.run(tb);
		printStats(bt);
	}
	private static void printStats(TbBacktestingFrame bt) {
		logger.info("-------------统计信息--------------");
		logger.info("-------------收益最高操作top10--------------");
		List topProfits = jdbc.queryForList(
				"select a.profit,b.c_stock_id,b.d_buy_date buy_days,b.d_sell_date "
				+ "from "
				+ "(SELECT     avg(a.d_sell_price) / avg(a.d_buy_price) profit,a.modelname , a.d_test_batch , a.c_stock_id , a.d_buy_date "
				+ "FROM    dat_model_buy a "
				+ "where  a.modelname=? and a.d_test_batch=? and a.d_sell_date is not null "
				+ "group by a.modelname , a.d_test_batch , a.c_stock_id , a.d_buy_date "
				+ "order by profit desc limit 10) a ,dat_model_buy b "
				+ "where a.modelname = b.modelname and a.d_test_batch = b.d_test_batch "
				+ "and a.c_stock_id = b.c_stock_id and a.d_buy_date = b.d_buy_date group by b.c_stock_id , b.d_buy_date order by a.profit desc"
				,
				new Object[]{ bt.modelName,bt.testBatch });
		for(Iterator it=topProfits.iterator();it.hasNext();){
			Map m = (Map) it.next();
			logger.info(m.get("c_stock_id") +"\t"+ m.get("profit")+"\t买入日期:"+m.get("buy_days")+"\t卖出日期"+m.get("d_sell_date"));
		}
		logger.info("-------------收益最低操作top10--------------");
		List toploss = jdbc.queryForList(
				"select a.profit,b.c_stock_id,b.d_buy_date buy_days,b.d_sell_date "
				+ "from "
				+ "(SELECT     avg(a.d_sell_price) / avg(a.d_buy_price) profit,a.modelname , a.d_test_batch , a.c_stock_id , a.d_buy_date "
				+ "FROM    dat_model_buy a where  a.modelname=? and a.d_test_batch=? and a.d_sell_date is not null "
				+ "group by a.modelname , a.d_test_batch , a.c_stock_id , a.d_buy_date "
				+ "order by profit limit 10) a ,dat_model_buy b "
				+ "where a.modelname = b.modelname and a.d_test_batch = b.d_test_batch "
				+ "and a.c_stock_id = b.c_stock_id and a.d_buy_date = b.d_buy_date group by b.c_stock_id , b.d_sell_date order by a.profit "
				,
				new Object[]{ bt.modelName,bt.testBatch });
		for(Iterator it=toploss.iterator();it.hasNext();){
			Map m = (Map) it.next();
			logger.info(m.get("c_stock_id") +"\t"+ m.get("profit")+"\t买入日期:"+m.get("buy_days")+"\t卖出日期"+m.get("d_sell_date"));
		}
		logger.info("-------------出现买入信号次数top10--------------");
		List topDays = jdbc.queryForList("SELECT  count(1) cnt,d_buy_date FROM dat_model_buy a"
				+ " where modelname=? and d_test_batch=?"
				+ "  group by a.modelname , a.d_test_batch , a.d_buy_date order by cnt desc limit 10 ",
				new Object[]{ bt.modelName,bt.testBatch });
		for(Iterator it=topDays.iterator();it.hasNext();){
			Map m = (Map) it.next();
			logger.info("买入股票数"+m.get("cnt")+"\t 买入日期"+m.get("d_buy_date"));
		}
	}
	/**
	 * 回测模型，需要按日期回测，首先计算当天所有股票是否有出现买卖点，然后从当天出现买点的股票中根据算法选择最优值。
	 * @param stockModel
	 */
	public void run(StockModel stockModel){
		//生成回测时间段的每一个交易日列表 
//		List<String> dateRange = DateUtil.getRange(stock,dateBegin,dateEnd);
		List<String> dateRange = jdbc.queryForList("select distinct d_date from stock_day where  d_date between ? and ?",
				new Object[]{ dateBegin,dateEnd},String.class);
		if(dateRange!=null&&dateRange.size()!=0){
			for(String date : dateRange){
				logger.debug("testing : "+date);
				stockList = jdbc.queryForList("select distinct stock_id from stock_day where  d_date = ?  ",
						new Object[]{ date },String.class);
				for(String stockId:stockList){
					stockModel= new ThirdBuyModel();
					stockModel.setDate(date);
					stockModel.setStock(stockId);
					//sell
					if(inventory.hasStock(stockId)&&stockModel.hasSell(inventory.getCostPrice(stockId))){
						float costPrice = inventory.getCostPrice(stockId);
						String date1 = DateUtil.getNextTradeDay(date,stockId);
						if(date1!=null){
							float price1 = PriceUtil.getNextDayPrice(date,stockId);
							if(inventory.sellOutStock(stockId, price1, date1)){
								logger.info(stockId+"与"+date1+"卖出"+";当前账户金额 "+inventory.getTotalVal(date));
								logger.info(stockId+"成本"+costPrice+"卖出价"+price1);
								jdbc.update(" update dat_model_buy set d_sell_date=?,"
										+ "d_sell_price=? where "
										+ "c_stock_id=? and d_sell_date is null",
										new Object[]{ date1,price1,stockId });
								if(price1>costPrice){
									wins+=1;
								} else{
									lose+=1;
								}
							}
						} else {
							logger.info(stockId+"卖点出现，但是无法获取下个交易日");
						}
	
					} 
					stockModel= new ThirdBuyModel();
					stockModel.setDate(date);
					stockModel.setStock(stockId);
					if(stockModel.hasBuy()){
						String date1 = DateUtil.getNextTradeDay(date,stockId);
						if(date1!=null){
							float price1 = PriceUtil.getNextDayPrice(date,stockId);
							jdbc.update(" insert into dat_model_buy(modelname,d_test_batch,d_buy_date,c_stock_id,d_buy_price,n_buy_amount)"
									+ "values(?,?,?,?,?,?)",
									new Object[]{ this.modelName,this.testBatch,date1,stockId,price1,Params.THE_COUNT });
							if(inventory.addStock(stockId, Params.THE_COUNT, price1, date1)){
								logger.info(stockId+"与"+date1+" 买入 "+";当前账户金额 "+inventory.getTotalVal(date));
							}else{
								logger.debug(stockId+"与"+date1+" 买点出现，但是资金不够 "+";当前账户现金 "+inventory.getCash());
							}
						} else {
							logger.info(stockId+"买点出现，但是无法获取下个交易日");
						}
					}
				}
				logger.info(inventory.getAccountDetail(date));
			}
		}
		logger.info("wins"+wins);
		logger.info("lose"+lose);
		logger.info(inventory);
	}
			
				

}
