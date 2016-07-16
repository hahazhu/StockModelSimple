/**
 * 
 */
package util;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author hahazhu
 *
 */
public class BeanFactory {
	private static BeanFactory instance = new BeanFactory();
	private BeanFactory(){
		
	}
	public static BeanFactory getInstance(){
		return instance;
	}

	private static final ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("local-config.xml");  
//	private static final ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("bae-db-config.xml");  

	public Object getBean(String name){
		return ac.getBean(name);
	}

}
