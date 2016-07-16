/**
 *
 */
package com.stock.data.stockprice;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

import com.stock.util.Params;

/**
 * @author hahazhu
 */
public class StockPriceWareHouse {
    private static JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean(
            "jdbcTemplate");
    private static Map allStockPrice = new ConcurrentHashMap();
    private static Map allThirtyStockPrice = new ConcurrentHashMap();

    private static Logger logger = Logger.getLogger("StockPriceWareHouse");
    private StockPriceWareHouse swh = new StockPriceWareHouse();

    private static int backCount = 0;

    public static void setBackCount(int i) {
        backCount = i;
    }

    private StockPriceWareHouse() {

    }

    public StockPriceWareHouse getInstance() {
        return swh;
    }

    public static void clearDayMemory() {
        allStockPrice.clear();
    }
    public static void clearThirtyMemory() {
        allThirtyStockPrice.clear();
    }

    public static void main(String[] args) {
        backCount = 10;
        getStockPriceMap("600019");
    }

    /**
     * @param stock
     * @return 返回这个股票从回测开始日期到现在的所有交易数据, map的key为日期，value为存储所有信息的map
     */
    public static Map getStockPriceMap(String stock) {
        if (!allStockPrice.containsKey(stock)) {
            logger.info("init day price !");
            initPriceMap();
            logger.info("stock price done!");
        }
        return (Map) allStockPrice.get(stock);
    }
    /**
     * @param stock
     * @return 返回这个股票从回测开始日期到现在的所有30分钟交易数据, map的key为日期，value为存储所有信息的map
     */
    public static Map getStockThirtyPriceMap(String stock) {
        if (!allThirtyStockPrice.containsKey(stock)) {
            logger.info("init 30k price");
            initThirtyPriceMap();
            logger.info("30k stock price done!");
        }
        return (Map) allThirtyStockPrice.get(stock);
    }

    public static void initPriceMap() {
        logger.info("init date price info");
        List<Map<String, Object>> prices = jdbc.queryForList("select stock_id,open,high,low,ma5,ma10,macd,d_date,"
                + "close,percent,volume from stock_day where "
                + "d_date between date_sub(?,interval ? day) and ?  order by stock_id,d_date", new Object[]{
                Params.dateBegin, backCount, Params.dateEnd
        });
        for (Iterator it = prices.iterator(); it.hasNext(); ) {
            Map m = (Map) it.next();
            String id = (String) m.get("stock_id");
            Map tmp = (allStockPrice.containsKey(id) ? (Map) allStockPrice.get(id) : new HashMap());
            tmp.put(m.get("d_date"), m);
            allStockPrice.put(id, tmp);
        }
        logger.info("init date price info done!!!!!");
    }

    public static void initThirtyPriceMap() {
        logger.info("init 30k price info");
        List<Map<String, Object>> prices = jdbc.queryForList("select stock_id,open,high,low,ma5,ma10,macd,t_time d_date,"
                + "close,percent,volume from stock_thirty where "
                + "date_format(t_time,'%Y%m%d') between date_sub(?,interval ? day) and ?  order by stock_id,t_time", new Object[]{
                Params.dateBegin, backCount, Params.dateEnd
        });
        for (Iterator it = prices.iterator(); it.hasNext(); ) {
            Map m = (Map) it.next();
            String id = (String) m.get("stock_id");
            Map tmp = (allStockPrice.containsKey(id) ? (Map) allStockPrice.get(id) : new HashMap());
            tmp.put(m.get("d_date"), m);
            allThirtyStockPrice.put(id, tmp);
        }
        logger.info("init 30k price info done!!!!!!!");
    }

    /**
     * @param stock
     * @param dateList
     * @return 返回该股票该日期段内的股价等信息
     */
    public static List getStockInfoList(String stock, List dateList) {
        Map stockPriceMap = getStockPriceMap(stock);
        List result = new LinkedList();
        if (dateList == null)
            return result;
        for (Iterator it = dateList.iterator(); it.hasNext(); ) {
            String date = (String) it.next();
            Map tmp = (Map) stockPriceMap.get(date);
            result.add(tmp);
        }
        return result;
    }
    /**
     * @param stock
     * @param timeList
     * @return 返回该股票该日期段内的股价等信息
     */
    public static List getStockThirtyInfoList(String stock, List timeList) {
        Map stockPriceMap = getStockThirtyPriceMap(stock);
        List result = new LinkedList();
        if (timeList == null)
            return result;
        for (Iterator it = timeList.iterator(); it.hasNext(); ) {
            String date = (String) it.next();
            Map tmp = (Map) stockPriceMap.get(date);
            result.add(tmp);
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
    /**
     * @param stock
     * @param time
     * @return 返回该时点开盘价
     */
    public static Map getStockThirtyInfoByTime(String stock, String time) {
        // TODO Auto-generated method stub
        Map m = getStockThirtyPriceMap(stock);
        Map values = (Map) m.get(time);
        return values;

    }

}
