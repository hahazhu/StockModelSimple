/**
 * 
 */
package com.stock;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author hahazhu
 *
 */
public class DailyNoti {
	public static void main(String[] args){
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("daily-noti.xml");  
	}

}
