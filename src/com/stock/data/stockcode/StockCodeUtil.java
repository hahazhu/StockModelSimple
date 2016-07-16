package com.stock.data.stockcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StockCodeUtil {
	final static int PAGE_NUM = 35;
	final static int SZ_PAGE_NUM = 50;
	private static Logger logger = Logger.getLogger("StockCodeUtil");
	public static List getStockListFromSina() throws IOException {
		// TODO Auto-generated method stub
		List<String> codes = new ArrayList<String>() ;  
        URL url = null ;  
		for(int i=1; i <= PAGE_NUM ; i ++ ){  
			logger.info("before "+i);
             url = new URL("http://vip.com.stock.finance.sina.com.cn/q/go.php/vInvestConsult/kind/qgqp/index.phtml?s_i=&s_a=&s_c=&s_t=&s_z=&p="+i) ;
			logger.info("after "+i);
             String code = getBatchStackCodes(url) ;  
             if(code.contains("sh")||code.contains("sz")){
				logger.info("code "+code);
            	 codes.addAll(handleStockCode(code)) ;
            	 
             }else{
             }
        }  
        
    	for(int i=1; i <= SZ_PAGE_NUM ; i ++ ){  
    		url = new URL("http://vip.com.stock.finance.sina.com.cn/q/go.php/vInvestConsult/kind/qgqp/index.phtml?s_i=&s_a=&s_c=&s_t=sz_a&s_z=&p="+i) ;
    		URLConnection connection = url.openConnection() ;  
    		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream())) ;  
    		String line = null ;  
    		StringBuffer sb = new StringBuffer() ;  
    		boolean flag =false ;  
    		while((line = br.readLine()) != null ){  
    			sb.append(line) ;  
    		}  
    		if(br != null ){  
    			br.close() ;  
    			br= null ;  
    		}    
    		
        }  

        return codes ;  
		
	}
	public static List getStockListFromEastMoney() throws IOException {
		// TODO Auto-generated method stub
		Set<String> codes = new TreeSet<String>() ;  
        URL url = null ;  
		for(int i=1; i <= 5 ; i ++ ){  
	        StringBuffer sb = new StringBuffer() ;  
//             url = new URL("http://hqdigi2.eastmoney.com/EM_Quote2010NumericApplication/index.aspx?type=s&sortType=C&sortRule=-1&pageSize=1000&page="+i+"&jsName=quote_123&style=33&token=44c9d251add88e27b65ed86506f6e5da&_g=0.37288209485961965") ;   
             url = new URL("http://hqdigi2.eastmoney.com/EM_Quote2010NumericApplication/index.aspx?type=s&sortType=A&sortRule=1&pageSize=1000&page="+i+"&jsName=quote_123&style=33&token=44c9d251add88e27b65ed86506f6e5da&_g=0.15164401757241852") ;   
             URLConnection connection = url.openConnection() ; 
             BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream())) ;  
     		 String line = null ;  
     		 while((line = br.readLine()) != null ){  
     			 sb.append(line) ;  
     		 }  
     		 String codestr = sb.toString();
     		 String jsonStr = codestr.substring(codestr.indexOf("=")+1);
     		 try {
     			 JSONObject jb = new JSONObject(jsonStr);
     			 JSONArray jsonArray = jb.getJSONArray("rank");
     			 for(int j =0;j<jsonArray.length();j++){
     				 String stockinfo = (String) jsonArray.get(j);
     				 String code = stockinfo.substring(0,7);
     				 if(code.startsWith("6")){
     					 code = "sh"+code.substring(0,6);
     				 }else{
     					 code = "sz"+code.substring(0,6);
     				 }
     				 codes.add(code);
     			 }
     		 } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }  
        return new ArrayList(codes) ;  
		
	}
	public static void main(String[] args) throws IOException{
		getStockListFromEastMoney();
	}
//  返回的值是一个js代码段  包括指定url页面包含的所有股票代码  
   public static  String getBatchStackCodes(URL url) throws IOException{  
        URLConnection connection = url.openConnection() ;  
//        connection.setConnectTimeout(30000) ;  
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream())) ;  
        String line = null ;  
        StringBuffer sb = new StringBuffer() ;  
        boolean flag =false ;  
        while((line = br.readLine()) != null ){  
        	
            if(line.contains("<script language=\"JavaScript\" id=\"hq_data_id\"") || flag){  
                sb.append(line) ;  
                flag = true ;  
            }  
            if(line.contains("</script>")){  
                flag =false ;  
                if(sb.length() > 0 ){  
                    if(sb.toString().contains("hq_data_id") && sb.toString().contains("list=")){  
                        break ;  
                    }else{  
                        sb.setLength(0) ;  
                    }  
                }  
            }  
        }  
        if(br != null ){  
            br.close() ;  
            br= null ;  
        }  
       return sb.toString() ;  
   } 
// 解析一组股票代码字符串   把code中包括的所有股票代码放入List中  
   public static List<String> handleStockCode(String code){  
       List<String> codes = null ;  
       int end = code.lastIndexOf("\"") ;  
           code = code.substring(0,end) ;  
       int start = code.lastIndexOf("list=") ;  
          code = code.substring(start) ;  
          code = code.substring(5) ;  
          codes = Arrays.asList(code.split(",")) ;  
       return codes ;  
   }
}
