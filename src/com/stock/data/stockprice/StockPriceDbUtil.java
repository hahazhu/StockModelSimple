package com.stock.data.stockprice;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

public class StockPriceDbUtil {
	private static Logger logger = Logger.getLogger("StockPriceDbUtil");
	private static String batchSql = "insert into stock_day "
			+ "(stock_id,market,open,close,high,low,volume,d_date) values(?,?,?,?,?,?,?,date_format(?,'%Y%m%d'))";
	private static JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance()
			.getBean("jdbcTemplate");

	public static void importDailyPrice(List content) {
		String sql = "delete FROM stock_day where d_date=date_format(?,'%Y%m%d')";
		Object[] stockInfo = (Object[]) content.get(0);
		jdbc.update(sql, new Object[] { stockInfo[7].toString() });
		jdbc.batchUpdate(batchSql, content);

	}

	public static void calDailyPrice(List codes, String date)
			throws SQLException {
		// TODO Auto-generated method stub
		for (int i = 0; i < codes.size(); i++) {
			String code = codes.get(i).toString().substring(2);
			try{
				String call = "upDayStockInfo";
				List list = new ArrayList();
				int noTradeFlag = jdbc
						.update("delete from stock_day  where d_date=? and stock_id = ? and  volume =0",
								new Object[] { date, code });
				if (noTradeFlag >= 1) {
					logger.info(code + " 今日无交易");
				} else {
					int count = jdbc
							.queryForInt(
									"select count(1) from (SELECT 1 FROM stock_day t where t.d_date<=? and t.stock_id = ? limit 2) a",
									new Object[] { date, code });
					if (count == 1) {
						jdbc.update(
								"update stock_day t set t.chg=0,t.percent=0,t.ma5=t.close,t.ma10=t.close,"
										+ "t.ma20=t.close,t.ma30=t.close,t.ema12=t.close ,t.ema26=t.close,t.dif=0,t.dea=0,t.macd=0  "
										+ "where t.d_date=? and t.stock_id = ? ",
								new Object[] { date, code });
					} else if (count > 1) {
						Map lastdayInfo = jdbc
								.queryForMap(
										"select t.d_date,t.close,t.ema12,t.ema26,t.dif,t.dea "
												+ "from stock_day t where t.d_date"
												+ "=(select max(x.d_date) from stock_day x where x.d_date<? and x.stock_id = ? ) and t.stock_id = ? limit 1",
										new Object[] { date, code, code });
						Map todayInfo = jdbc.queryForMap(
								"select t.close from stock_day t "
										+ "where t.d_date=? and t.stock_id = ? ",
								new Object[] { date, code });
						jdbc.update(
								"UPDATE stock_day t set t.chg= (?-?) ,"
										+ "t.percent=(100*(?-?)/?) where t.d_date=? and t.stock_id = ? ",
								new Object[] { todayInfo.get("close"),
										lastdayInfo.get("close"),
										todayInfo.get("close"),
										lastdayInfo.get("close"),
										lastdayInfo.get("close"), date, code });
						List closeList = jdbc
								.queryForList(
										"select x.close close  from stock_day x where x.d_date<= ? "
												+ "and x.stock_id = ? order by x.d_date desc limit 30",
										new Object[] { date, code });
	
						jdbc.update(
								"update stock_day t "
										+ "set"
										+ "    t.ma5 = ?,"
										+ "    t.ma10 = ?,"
										+ "    t.ma20 = ?,"
										+ "    t.ma30 = ?"
										+ " where   t.d_date = ? and t.stock_id = ? ",
								new Object[] { getMA(closeList, 5),
										getMA(closeList, 10), getMA(closeList, 20),
										getMA(closeList, 30), date, code });
						Float ema12 = Float.valueOf(lastdayInfo.get("ema12")
								.toString())
								* 11
								/ 13
								+ Float.valueOf(todayInfo.get("close").toString())
								* 2 / 13;
						Float ema26 = Float.valueOf(lastdayInfo.get("ema26")
								.toString())
								* 25
								/ 27
								+ Float.valueOf(todayInfo.get("close").toString())
								* 2 / 27;
						Float dif = ema12 - ema26;
						Float dea = Float
								.valueOf(lastdayInfo.get("dea").toString())
								* 8
								/ 10 + dif * 2 / 10;
						Float macd = 2 * (dif - dea);
						jdbc.update(
								"UPDATE stock_day t set t.ema12= ? ,t.ema26= ? ,t.dif= ? ,t.dea= ? ,t.macd= ?"
										+ "						  where t.d_date=? and t.stock_id = ? ",
								new Object[] { ema12, ema26, dif, dea, macd, date,
										code });
					}
	
					// jdbc.execute("call upDayStockInfo('" + code + "'," + "'" +
					// date
					// + "')");
					logger.info(codes.get(i).toString());
				}
			}
			catch(Exception e){
				e.printStackTrace();
				logger.error(code+e.toString());
			}
		}
		
	}
	public static void calDailyPriceByCode(String code, String date)
			throws SQLException {
		// TODO Auto-generated method stub
        try{
            String call = "upDayStockInfo";
            List list = new ArrayList();
            int noTradeFlag = jdbc
                    .update("delete from stock_day  where d_date=? and stock_id = ? and  volume =0",
                            new Object[] { date, code });
            if (noTradeFlag >= 1) {
                logger.info(code + " 今日无交易");
            } else {
                int count = jdbc
                        .queryForInt(
                                "select count(1) from (SELECT 1 FROM stock_day t where t.d_date<=? and t.stock_id = ? limit 2) a",
                                new Object[] { date, code });
                if (count == 1) {
                    jdbc.update(
                            "update stock_day t set t.chg=0,t.percent=0,t.ma5=t.close,t.ma10=t.close,"
                                    + "t.ma20=t.close,t.ma30=t.close,t.ema12=t.close ,t.ema26=t.close,t.dif=0,t.dea=0,t.macd=0  "
                                    + "where t.d_date=? and t.stock_id = ? ",
                            new Object[] { date, code });
                } else if (count > 1) {
                    Map lastdayInfo = jdbc
                            .queryForMap(
                                    "select t.d_date,t.close,t.ema12,t.ema26,t.dif,t.dea "
                                            + "from stock_day t where t.d_date"
                                            + "=(select max(x.d_date) from stock_day x where x.d_date<? and x.stock_id = ? ) and t.stock_id = ? limit 1",
                                    new Object[] { date, code, code });
                    Map todayInfo = jdbc.queryForMap(
                            "select t.close from stock_day t "
                                    + "where t.d_date=? and t.stock_id = ? ",
                            new Object[] { date, code });
                    jdbc.update(
                            "UPDATE stock_day t set t.chg= (?-?) ,"
                                    + "t.percent=(100*(?-?)/?) where t.d_date=? and t.stock_id = ? ",
                            new Object[] { todayInfo.get("close"),
                                    lastdayInfo.get("close"),
                                    todayInfo.get("close"),
                                    lastdayInfo.get("close"),
                                    lastdayInfo.get("close"), date, code });
                    List closeList = jdbc
                            .queryForList(
                                    "select x.close close  from stock_day x where x.d_date<= ? "
                                            + "and x.stock_id = ? order by x.d_date desc limit 30",
                                    new Object[] { date, code });

                    jdbc.update(
                            "update stock_day t "
                                    + "set"
                                    + "    t.ma5 = ?,"
                                    + "    t.ma10 = ?,"
                                    + "    t.ma20 = ?,"
                                    + "    t.ma30 = ?"
                                    + " where   t.d_date = ? and t.stock_id = ? ",
                            new Object[] { getMA(closeList, 5),
                                    getMA(closeList, 10), getMA(closeList, 20),
                                    getMA(closeList, 30), date, code });
                    Float ema12 = Float.valueOf(lastdayInfo.get("ema12")
                            .toString())
                            * 11
                            / 13
                            + Float.valueOf(todayInfo.get("close").toString())
                            * 2 / 13;
                    Float ema26 = Float.valueOf(lastdayInfo.get("ema26")
                            .toString())
                            * 25
                            / 27
                            + Float.valueOf(todayInfo.get("close").toString())
                            * 2 / 27;
                    Float dif = ema12 - ema26;
                    Float dea = Float
                            .valueOf(lastdayInfo.get("dea").toString())
                            * 8
                            / 10 + dif * 2 / 10;
                    Float macd = 2 * (dif - dea);
                    jdbc.update(
                            "UPDATE stock_day t set t.ema12= ? ,t.ema26= ? ,t.dif= ? ,t.dea= ? ,t.macd= ?"
                                    + "						  where t.d_date=? and t.stock_id = ? ",
                            new Object[] { ema12, ema26, dif, dea, macd, date,
                                    code });
                }

                logger.info(code);
            }
        }
        catch(Exception e){
            e.printStackTrace();
            logger.error(code+e.toString());
        }

	}

	private static float getMA(List price, int days) {
		float ma = 0;
		
		for (int i = 0; i < (days>price.size()?price.size():days); i++) {
			Map m = (Map) price.get(i);
			ma = ma + ((BigDecimal) m.get("CLOSE")).floatValue();
		}
		return ma / days;

	}

}
