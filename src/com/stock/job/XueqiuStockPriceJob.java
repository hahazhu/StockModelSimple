/**
 * 
 */
package com.stock.job;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

import com.stock.util.DataUtil;

/**
 * @author hahazhu
 * 
 */
public class XueqiuStockPriceJob implements Runnable {
	Logger logger = Logger.getLogger(this.getClass().getName());

	private String stockCode;
	private static String sql = "insert into stock_day_tmp(stock_id,market,volume,open,high,close,low,chg,percent,turnrate,ma5,ma10,ma20,ma30,ema12,ema26,dif,dea,macd,d_date) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public XueqiuStockPriceJob(String stockCode) {
		this.stockCode = stockCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean("jdbcTemplate");
		long start = System.currentTimeMillis();
		logger.info("get " + stockCode );
		start = System.currentTimeMillis();
		List stockAllHisInfo = new ArrayList();
		URL url;
		try {
			url = new URL(
					"http://www.xueqiu.com/stock/forchartk/stocklist.json?period=1day&symbol="
							+ stockCode + "&type=before&_=1460389761298");

			int n = 0;
			StringBuffer sb = new StringBuffer();
			while (n == 0) {
				try {
					HttpURLConnection connection = (HttpURLConnection) url
							.openConnection();
					connection.setConnectTimeout(3000);
					connection.setReadTimeout(10000);
					connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
					connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.94 Safari/537.36");
					connection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
					connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
					connection.setRequestProperty("Host", "xueqiu.com");
					connection.setRequestProperty("Proxy-Connection", "keep-alive");
					connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
					connection
							.setRequestProperty(
									"Cookie",
									"s=1e8c1213rb; bid=e3e1aafc190e65e60dca5d70b5076471_io9xpiby; snbim_minify=true; webp=0; xq_a_token=d0ffb1c343ed5691d9fa200a2a26cd4423be68c1; xqat=d0ffb1c343ed5691d9fa200a2a26cd4423be68c1; xq_r_token=a3a5e1a29bab2ecd1d695d6bb58e34597dd15c99; xq_is_login=1; u=9803841032; xq_token_expire=Mon%20Jul%2011%202016%2022%3A37%3A48%20GMT%2B0800%20(CST); __utmt=1; __utma=1.741705998.1463398616.1465111023.1466087899.15; __utmb=1.1.10.1466087899; __utmc=1; __utmz=1.1463398616.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); Hm_lvt_1db88642e346389874251b5a1eded6e3=1466087872; Hm_lpvt_1db88642e346389874251b5a1eded6e3=1466087899");
					connection.connect();
					
					InputStream in = connection.getInputStream();
		            in = new GZIPInputStream(in);
		            BufferedReader bd = new BufferedReader(new InputStreamReader(in));
		            String text;
		            while ((text = bd.readLine()) != null) sb.append(text);
		            connection.disconnect();
					
//					BufferedReader br = new BufferedReader(
//							new InputStreamReader(connection.getInputStream()));
//					String line = null;
//					while ((line = br.readLine()) != null) {
//						sb.append(line);
//					}
//					connection.disconnect();
					n = 1;
				} catch (Exception e) {
					logger.error(stockCode+" connect lose.....sleeping 10s");
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						logger.error(e1.toString());
					}
				}
			}
			JSONObject jb = new JSONObject(sb.toString());
			JSONArray object = jb.getJSONArray("chartlist");
			double lastEma12 = 0;
			double lastEma26 = 0;
			double lastDea = 0;
			for (int i = 0; i < object.length(); i++) {
				List stockInfo = new ArrayList();
				stockInfo.add(stockCode.substring(2));
				stockInfo.add(stockCode.substring(0, 2));
				JSONObject object2 = (JSONObject) object.get(i);
				BigDecimal db = new BigDecimal(object2.get("volume").toString());
				String volume = db.toPlainString().toString();
				String open = object2.get("open").toString();
				String high = object2.get("high").toString();
				String close = object2.get("close").toString();
				String low = object2.get("low").toString();
				String chg = object2.get("chg").toString();
				String percent = object2.get("percent").toString();
				String turnrate = object2.get("turnrate").toString();
				String ma5 = object2.get("ma5").toString();
				String ma10 = object2.get("ma10").toString();
				String ma20 = object2.get("ma20").toString();
				String ma30 = object2.get("ma30").toString();
				String dif = object2.get("dif").toString();
				String dea = object2.get("dea").toString();
				String macd = object2.get("macd").toString();
				String time = DataUtil.getDate8FromString(
						(String) object2.get("time")).toString();
				stockInfo.add(volume);
				stockInfo.add(open);
				stockInfo.add(high);
				stockInfo.add(close);
				stockInfo.add(low);
				stockInfo.add(chg);
				stockInfo.add(percent);
				stockInfo.add(turnrate);
				stockInfo.add(ma5);
				stockInfo.add(ma10);
				stockInfo.add(ma20);
				stockInfo.add(ma30);
				if (i == 0) {
					stockInfo.add(close);
					stockInfo.add(close);
					stockInfo.add("0");
					stockInfo.add("0");
					stockInfo.add("0");
					lastEma12 = Double.parseDouble(close);
					lastEma26 = Double.parseDouble(close);
					lastDea = 0;
				} else {
					List result = DataUtil
							.getMacd(lastEma12, lastEma26, lastDea, Double
									.parseDouble(object2.get("close")
											.toString()));
					stockInfo.add(result.get(0));
					stockInfo.add(result.get(1));
					stockInfo.add(result.get(2));
					stockInfo.add(result.get(3));
					stockInfo.add(result.get(4));
					lastEma12 = Double.parseDouble(result.get(0).toString());
					lastEma26 = Double.parseDouble(result.get(1).toString());
					lastDea = Double.parseDouble(result.get(3).toString());
				}
				stockInfo.add(time);
				stockAllHisInfo.add(stockInfo.toArray());
			}
			logger.info("download "+stockCode + " done!");

			long cost = System.currentTimeMillis() - start;
			logger.info("download " + stockCode + " cost:" + cost);
			start = System.currentTimeMillis();
            jdbc.batchUpdate(sql, stockAllHisInfo);
			cost = System.currentTimeMillis() - start;
			logger.info("insert " + stockCode + " cost:" + cost);
		} catch (MalformedURLException e1) {
			logger.error(e1.toString());
		} catch (NumberFormatException e) {
			logger.error(e.toString());
		} catch (JSONException e) {
			logger.error(e.toString());
		}
	}

}
