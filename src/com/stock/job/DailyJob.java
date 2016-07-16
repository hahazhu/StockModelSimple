package com.stock.job;

import com.stock.data.stockprice.StockPriceDbUtil;
import com.stock.data.stockprice.StockPriceUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import util.BeanFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by lll on 2016/5/24.
 */
public class DailyJob implements Runnable {
    String code ;
    private static String batchSql = "insert into stock_day "
            + "(stock_id,market,open,close,high,low,volume,d_date) values(?,?,?,?,?,?,?,date_format(?,'%Y%m%d'))";
    public DailyJob(String code) {
        this.code = code;
    }

    @Override
    public void run() {
        try {
            JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean("jdbcTemplate");
            List stockInfoArr = (List) StockPriceUtil.getStockInfoByCode(code);
            String sql = "delete FROM stock_day where d_date=date_format(?,'%Y%m%d') and stock_id=?";
            jdbc.update(sql, new Object[] { stockInfoArr.get(7), stockInfoArr.get(0)});
            jdbc.update(batchSql, stockInfoArr.toArray());

            String date10 = (String) stockInfoArr.get(7);
            StockPriceDbUtil.calDailyPriceByCode(code.substring(2), date10.substring(0, 4) + date10.substring(5, 7) + date10.substring(8, 10));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
