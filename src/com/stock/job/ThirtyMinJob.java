/**
 * 
 */
package com.stock.job;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;

import util.BeanFactory;

import com.stock.util.DataUtil;

/**
 * @author hahazhu
 *
 */
public class ThirtyMinJob implements Runnable{
	private static Logger logger = Logger.getLogger("ThirtyMinJob");

	private String stockCode;
	static String sql = "insert into stock_thirty(stock_id,market,open,high,close,low,volume,chg,percent,turnrate,ma5,ma10,ma20,ema12,ema26,dif,dea,macd,t_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public ThirtyMinJob (String stockCode) {
		this.stockCode = stockCode;
	}

	public static void main(String[] args) throws Exception{
	}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			getAllHisStockInfoByCode(stockCode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void getAllHisStockInfoByCode(String stockCode) throws Exception {
		JdbcTemplate jdbc = (JdbcTemplate) BeanFactory.getInstance().getBean("jdbcTemplate");
		List stockAllHisInfo = new ArrayList();
		URL url = new URL(
				"http://api.finance.ifeng.com/akmin?scode="
						+ stockCode + "&type=30");
		logger.info(stockCode);

		long start = System.currentTimeMillis();
		int n=0;
		StringBuffer sb = new StringBuffer();
		while(n==0){
			try {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(10000);
				connection.setReadTimeout(5000);
				connection
				.setRequestProperty(
						"Cookie",
						"vjuids=-34559dd01.149c84f40d7.0.6e2d2f92; userid=1416404878018_ufe8j03248; prov=cn021; city=021; weather_city=sh; region_ip=222.44.86.x; region_ver=1.2; ifengRotator_AP1998=1; user_saw_channel_map=%2Cstock%3A%u80A1%u7968%3A1425477065594; user_saw_stock_map=%2Csh600718%3A%u4E1C%u8F6F%u96C6%u56E2%3A1416495311254%2Csz000055%3A%u65B9%u5927%u96C6%u56E2%3A1417185129381%2Csz000556%3APT%u5357%u6D0B%3A1417189892757%2Csz000557%3A*ST%u5E7F%u590F%3A1417190092104%2Csh600000%3A%u6D66%u53D1%u94F6%u884C%3A1425477091651; vjlast=1416404878.1425477049.21; js_pop_float=true; BIGipServerpool_caijing_flash=471474186.20480.0000; HOT_TAG=n; READ_TAG=n");
				connection.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				connection.disconnect();
				n=1;
			} catch (Exception e) {
				System.out.println("connect lose.....sleeping 10s");
				Thread.sleep(10000);
			}
		}
		if(sb!=null&&!sb.toString().equals("")){
			JSONObject jb = new JSONObject(sb.toString());
			JSONArray object = jb.getJSONArray("record");
			double lastEma12=0;double lastEma26=0;double lastDea=0;
			for (int i = 0; i < object.length(); i++) {
				List stockInfo = new ArrayList();
				stockInfo.add(stockCode.substring(2));
				stockInfo.add(stockCode.substring(0, 2));
				JSONArray object2 = (JSONArray) object.get(i);
				String time = (String) object2.get(0);
				
				String open = (String) object2.get(1);
				stockInfo.add(open);
				String high = (String) object2.get(2);
				stockInfo.add(high);
				String close = (String) object2.get(3);
				stockInfo.add(close);
				String low = (String) object2.get(4);
				stockInfo.add(low);
				try{
					Double volume = (Double) object2.get(5);
					stockInfo.add(volume);
				}catch(ClassCastException e){
					//e.printStackTrace();
					Integer volume = (Integer) object2.get(5);
					stockInfo.add(volume);
				}
				String chg = (String) object2.get(6);
				stockInfo.add(chg);
				try{
					Double percent = (Double) object2.get(7);
					stockInfo.add(percent);
				}catch(ClassCastException e){
					//e.printStackTrace();
					Integer percent = (Integer ) object2.get(7);
					stockInfo.add(percent);
				}
				try{
					Double turnrate = (Double) object2.get(14);
					stockInfo.add(turnrate);
				}catch(ClassCastException e){
					//e.printStackTrace();
					Integer turnrate= (Integer ) object2.get(14);
					stockInfo.add(turnrate);
				}
				String ma5 = (String) object2.get(8);
				stockInfo.add(ma5);
				String ma10 = (String) object2.get(9);
				stockInfo.add(ma10);
				String ma20 = (String) object2.get(10);
				stockInfo.add(ma20);
				if(i==0){
					stockInfo.add(close);
					stockInfo.add(close);
					stockInfo.add("0");
					stockInfo.add("0");
					stockInfo.add("0");
					lastEma12 =Double.parseDouble(close);
					lastEma26 = Double.parseDouble(close);
					lastDea = 0;
				}else{
					List result=DataUtil.getMacd(lastEma12, lastEma26, lastDea,Double.parseDouble(object2.get(3).toString()));
					stockInfo.add(result.get(0));
					stockInfo.add(result.get(1));
					stockInfo.add(result.get(2));
					stockInfo.add(result.get(3));
					stockInfo.add(result.get(4));
					lastEma12 =Double.parseDouble(result.get(0).toString());
					lastEma26 =Double.parseDouble(result.get(1).toString());
					lastDea = Double.parseDouble(result.get(3).toString());
				}
				stockInfo.add(time);
				stockAllHisInfo.add(stockInfo.toArray());
			}
		}
		long cost = System.currentTimeMillis() - start;
		logger.info("download " + stockCode + " cost:" + cost);
		start = System.currentTimeMillis();
		jdbc.batchUpdate(sql,stockAllHisInfo);
		cost = System.currentTimeMillis() - start;
		logger.info("insert " + stockCode + " cost:" + cost);
	}
}
