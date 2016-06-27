package com.chinascope.cloud.excute;

import com.chinascope.cloud.entity.Job;
import com.chinascope.cloud.service.DemoService;
import com.chinascope.cloud.util.JavaLogging;
import org.slf4j.Logger;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public class DemoLogical extends DefaultExcutor {
    private static Logger log = JavaLogging.log();

    public DemoLogical() {
    }

    @Override
    public void service() {
        Job job = this.bizService().getJob();
        System.out.println("demoLogical Service " + job.getName());
        DemoService demoService = (DemoService) this.bizService();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.HOUR, c.get(Calendar.HOUR - 10));
        Date currentDate = new Date();
        System.out.println("last time:" + c.getTime() + "\t current Time" + currentDate);
        demoService.demoPartitionAnnotation(c.getTime(), currentDate);
        log.info("This is demo Service.");
    }
}
