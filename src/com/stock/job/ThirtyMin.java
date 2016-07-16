/**
 * 
 */
package com.stock.job;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * @author hahazhu
 * 
 */
public class ThirtyMin {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		URL url = new URL(
				"http://hq2fls.eastmoney.com/EM_Quote2010PictureApplication/Flash.aspx?Type=CHM30&ID=0000112&lastnum=11321&r=0.3050547023303807");
		URLConnection connection = url.openConnection();
		// connection.setConnectTimeout(30000) ;
		System.out.println(new String(decompress(connection.getInputStream())));
		StringBuffer sb = new StringBuffer();
		String line = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream())) ;  
		while((line = br.readLine()) != null ){  
			sb.append(line) ;  
		}  
		String codestr = sb.toString();
		
		System.err.println(codestr);

	}
	 public static byte[] decompress(InputStream is) {  
	        InflaterInputStream iis = new InflaterInputStream(is);  
	        ByteArrayOutputStream o = new ByteArrayOutputStream(1024);  
	        try {  
	            int i = 1024;  
	            byte[] buf = new byte[i];  
	  
	            while ((i = iis.read(buf, 0, i)) > 0) {  
	                o.write(buf, 0, i);  
	            }  
	  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
	        return o.toByteArray();  
	    }  
	/** 
     * 解压缩 
     *  
     * @param data 
     *            待压缩的数据 
     * @return byte[] 解压缩后的数据 
     */  
    public static byte[] decompress(byte[] data) {  
        byte[] output = new byte[0];  
  
        Inflater decompresser = new Inflater();  
        decompresser.reset();  
        decompresser.setInput(data);  
  
        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);  
        try {  
            byte[] buf = new byte[1024];  
            while (!decompresser.finished()) {  
                int i = decompresser.inflate(buf);  
                o.write(buf, 0, i);  
            }  
            output = o.toByteArray();  
        } catch (Exception e) {  
            output = data;  
            e.printStackTrace();  
        } finally {  
            try {  
                o.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
  
        decompresser.end();  
        return output;  
    }  
}
