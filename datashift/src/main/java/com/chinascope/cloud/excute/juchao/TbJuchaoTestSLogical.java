package com.chinascope.cloud.excute.juchao;

import com.chinascope.cloud.excute.DefaultExcutor;
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
        c.setTime(new Date());
        c.set(Calendar.HOUR, c.get(Calendar.HOUR - 10));
        Date date1 = c.getTime();
        Date currentDate = new Date();
        System.out.println("last time:" + c.getTime() + "\t current Time" + currentDate);
        this.bizService().fetchJuchaoDataService(date1, currentDate);
    }
}
