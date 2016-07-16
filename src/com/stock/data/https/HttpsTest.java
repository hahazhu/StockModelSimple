package com.stock.data.https;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpsTest {

    public static void main(String[] args) {
        new HttpsTest().doMain();
        log("DONE");
    }
    
    public void doMain() {
        String hsUrl = "https://xueqiu.com/stock/forchartk/stocklist.json?symbol=SH600051&period=1day&type=before&begin=1428846950952&end=1460382950952&_=1460382950953";
        URL url ;
        
        try {
            url = new URL(hsUrl);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setConnectTimeout(3000);
            con.setReadTimeout(10000);
            con
					.setRequestProperty(
							"Cookie",
							"s=frx16olz2j; bid=e3e1aafc190e65e60dca5d70b5076471_ifgtt52i; webp=1; xq_a_token=94e4d1108329136bfbefbe21e2b32f2e95f943f2; xqat=94e4d1108329136bfbefbe21e2b32f2e95f943f2; xq_r_token=6df2b216171e56c7fa05a7ab19013d9c2a170ae5; xq_is_login=1; u=9803841032; xq_token_expire=Tue%20Apr%2026%202016%2020%3A50%3A14%20GMT%2B0800%20(CST); snbim_minify=true; __utmt=1; Hm_lvt_1db88642e346389874251b5a1eded6e3=1459515004; Hm_lpvt_1db88642e346389874251b5a1eded6e3=1460382198; __utma=1.494815280.1444033728.1460297741.1460380867.84; __utmb=1.7.10.1460380867; __utmc=1; __utmz=1.1444033728.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");

            X509TrustManager xtm = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    // TODO Auto-generated method stub
                    return null;
                }
                
                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                    // TODO Auto-generated method stub
                    
                }
            };
            
            TrustManager[] tm = { xtm };
            
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tm, null);
            
            con.setSSLSocketFactory(ctx.getSocketFactory());
            con.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
            
            
            log(con.getResponseCode());
            log(con.getCipherSuite());
            log("");
            Certificate[] certs = con.getServerCertificates();
            
            int certNum = 1;
            
            for(Certificate cert : certs) {
                X509Certificate xcert = (X509Certificate) cert;
                log("Cert No. " + certNum ++);
                log(xcert.getType());
                log(xcert.getPublicKey().getAlgorithm());
                log(xcert.getIssuerDN());
                log(xcert.getIssuerDN());
                log(xcert.getNotAfter());
                log(xcert.getNotBefore());
                log("");
            }
            con.getInputStream();
			BufferedReader br = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    static void log(Object o) {
        System.out.println(o);
    }

}