package com.csf.cloud.excute.demo;

import com.csf.cloud.entity.Job;
import com.csf.cloud.excute.DefaultExcutor;
import com.csf.cloud.service.demo.DemoService;
import com.csf.cloud.util.BizException;
import com.csf.cloud.util.JavaLogging;
import org.slf4j.Logger;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public class DemoLogical extends DefaultExcutor<DemoService> {
    private static Logger log = JavaLogging.log();

    public DemoLogical() {
    }

    @Override
    public void service() throws BizException {
        Job job = this.bizService().getJob();
        System.out.println("demoLogical Service " + job.getName());
        DemoService demoService = this.bizService();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.set(Calendar.HOUR, c.get(Calendar.HOUR - 10));
        Date currentDate = new Date();
        System.out.println("last time:" + c.getTime() + "\t current Time" + currentDate);
        //demoService.demoPartitionAnnotation(c.getTime(), currentDate);
        demoService.demoPartitionIntegerAnnotation(1,7);

        log.info("This is demo Service.");
    }
}
