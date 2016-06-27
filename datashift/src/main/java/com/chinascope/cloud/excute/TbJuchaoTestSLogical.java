package com.chinascope.cloud.excute;

import com.chinascope.cloud.service.juchao.TbJuchaoTestSService;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by soledede.weng on 2016/6/27.
 */
public class TbJuchaoTestSLogical extends DefaultExcutor<TbJuchaoTestSService> {

    @Override
    public void service() {
        Calendar c = Calendar.getInstance();
        Date date1 = new Date();
        c.setTimeInMillis(date1.getTime());
        System.out.println("Thread 1 minutes....");
        try {
            Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Date date2 = new Date();
        c.setTimeInMillis(date2.getTime());
        this.bizService().fetchJuchaoDataService(date1, date2);
    }
}
