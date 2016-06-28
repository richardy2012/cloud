package com.csf.cloud.excute.juchao;

import com.csf.cloud.entity.test.Dog;
import com.csf.cloud.entity.test.Finger;
import com.csf.cloud.excute.DefaultExcutor;
import com.csf.cloud.service.juchao.TbJuchaoTestSService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
        //test for oracle mybatis
        this.bizService().fetchJuchaoDataService(date1, currentDate);
        //test for mongo
        List<Finger> figers = new ArrayList<Finger>();
        for (int i = 0; i < 10; i++) {
            figers.add(new Finger("finger" + i + 1));
        }
        this.bizService().saveDog(new Dog("zhuangzhuang", "white", figers));
    }
}
