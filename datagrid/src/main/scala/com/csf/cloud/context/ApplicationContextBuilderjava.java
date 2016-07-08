package com.csf.cloud.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by soledede.weng on 2016/7/8.
 */
public class ApplicationContextBuilderJava {

    private static ApplicationContext springContext = null;
    static Object lock = new Object();

    static {
        try {
            getApplicationContext();
        } catch (Exception e) {
            System.out.println("initialize AplicationContext failed!");
            e.printStackTrace();
        }
    }

    public static ApplicationContext getApplicationContext() {
        if (springContext == null) {
            synchronized (lock) {
                if (springContext == null) {
                    ClassPathXmlApplicationContext contex = new ClassPathXmlApplicationContext("config\\applicationContext_aop.xml");
                    springContext = contex;
                }
            }
        }
        return springContext;
    }

    public static Object getSpringContextBean(String beanName) {
        return springContext.getBean(beanName);
    }

}
