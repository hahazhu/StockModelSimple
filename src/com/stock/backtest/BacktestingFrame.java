/**
 * 
 */
package com.stock.backtest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

import com.stock.model.StockModel;
import com.stock.util.DateUtil;
import com.stock.util.Params;
import com.stock.util.PriceUtil;

/**
 * @author hahazhu
 *
 */
public class BacktestingFrame implements Runnable{
	/**
	 * 
	 */
	/**
	 * 
	 */
	Inventory inventory = null;
	Inventory inventoryWithInfiniteMoney = null;
	private String dateBegin;
	private String dateEnd;
	private String market;
	private List<String> stockList;
	private static Logger logger = Logger.getLogger("BacktestingFrame");
	private static JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean("jdbcTemplate");
	public int wins = 0;
	public int lose = 0;
	String modelName;
	String testBatch;
	StockModel stockModel;
	
	public BacktestingFrame(String dateBegin,String dateEnd,String market,int cash){
		this.dateBegin = dateBegin;
		this.dateEnd = dateEnd;
		this.market = market;
		inventory = new Inventory(cash);
	}
	public BacktestingFrame(String dateBegin,String dateEnd,List stockList,int cash,StockModel model,String testBatch){
		this.dateBegin = dateBegin;
		this.dateEnd = dateEnd;
		this.stockList = stockList;
		inventory = new Inventory(cash);
		this.modelName = model.getClass().getSimpleName();
		this.testBatch = testBatch;
		this.stockModel = model;
	}
	public static void main(String[] args) throws InterruptedException{
		
		
	}
	
	/**
	 * 回测模型，需要按日期回测，首先计算当天所有股票是否有出现买卖点，然后从当天出现买点的股票中根据算法选择最优值。
	 * @param stockModel
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		List<String> dateRange = jdbc.queryForList("select distinct d_date from stock_day where d_date between ? and ?",
				new Object[]{ dateBegin,dateEnd},String.class);
		if(dateRange!=null&&dateRange.size()!=0){
			for(String date : dateRange){
				logger.debug("testing : "+date);
				stockList = jdbc.queryForList("select distinct stock_id from stock_day where d_date = ?  ",
						new Object[]{ date },String.class);
				stockModel.setDate(date);
				for(String stockId:stockList){
					stockModel.setStock(stockId);
					if(inventory.hasStock(stockId)&&stockModel.hasSell(inventory.getCostPrice(stockId))){
						float costPrice = inventory.getCostPrice(stockId);
						String date1 = DateUtil.getNextTradeDay(date,stockId);
						if(date1!=null){
							float price1 = PriceUtil.getNextDayPrice(date,stockId);
							jdbc.update(" update dat_model_buy set d_sell_date=?,"
									+ "d_sell_price=? where "
									+ "c_stock_id=? and d_sell_date is null",
									new Object[]{ date1,price1,stockId });
							if(inventory.sellOutStock(stockId, price1, date1)){
								logger.info(stockId+"与"+date1+"卖出"+";当前账户金额 "+inventory.getTotalVal(date));
							}
							if(price1>costPrice){
								wins+=1;
							} else{
								lose+=1;
							}
						} else {
							logger.info(stockId+"卖点出现，但是无法获取下个交易日");
						}
	
					} 
					if(stockModel.hasBuy()){
						String date1 = DateUtil.getNextTradeDay(date,stockId);
						if(date1!=null){
							float price1 = PriceUtil.getNextDayPrice(date,stockId);
							float cashToBuy = inventory.getCash()*Params.THE_PERCENT/100;
							int count =  Math.round(cashToBuy/price1)-Math.round(cashToBuy/price1)%100; 
							if(count<100){
								logger.info(stockId+"与"+date1+" 买点出现，但是资金不够 "+";当前账户现金 "+inventory.getCash());
							}else{
								jdbc.update(" insert into dat_model_buy(modelname,d_test_batch,d_buy_date,c_stock_id,d_buy_price,n_buy_amount)"
									+ "values(?,?,?,?,?,?)",
									new Object[]{ this.modelName,this.testBatch,date1,stockId,price1,count});
								if(inventory.addStock(stockId, count, price1, date1)){
									logger.info(stockId+"与"+date1+" 买入 "+";当前账户金额 "+inventory.getTotalVal(date));
								}else{
									logger.info(stockId+"与"+date1+" 买点出现，但是资金不够 "+";当前账户现金 "+inventory.getCash());
								}
							}
							
						} else {
							logger.info(stockId+"买点出现，但是无法获取下个交易日");
						}
					}
				}
				logger.info(inventory.getAccountDetail(date));
			}
		}
		Map testParam = new HashMap();
		testParam.put("start_date", dateBegin);
		testParam.put("end_date", dateEnd);
		testParam.put("modelName", modelName);
		testParam.put("batch", testBatch);
		testParam.put("return", inventory.getTotalVal(dateEnd)/Params.capital);
		testParam.put("wins", wins);
		testParam.put("lose", lose);
		testParam.put("holdPeriod", Params.holdPeriod);
		testParam.put("winPercent", (float)wins/(wins+lose));
		BacktestResult.testResult.add(testParam);
		logger.info("wins"+wins);
		logger.info("lose"+lose);
		logger.info(inventory);
	}
			
				

}
