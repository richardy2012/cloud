package com.chinascope.cloud.excute;

import com.chinascope.cloud.entity.Job;
import com.chinascope.cloud.service.DemoService;
import com.chinascope.cloud.util.JavaLogging;
import org.slf4j.Logger;

import java.util.Date;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public class DemoLogical extends DefaultExcutor {
    private static Logger log = JavaLogging.log();

    @Override
    public void service() {
        Job job = this.bizService().getJob();
        System.out.println("demoLogical Service " + job.getName());
        DemoService demoService = (DemoService) this.bizService();
        demoService.demoPartitionAnnotation(new Date(), new Date());
        log.info("This is demo Service.");
    }
}
