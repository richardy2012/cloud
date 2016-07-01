package com.csf.cloud.annotation;

import com.csf.cloud.context.ApplicationContextBuilder;
import com.csf.cloud.service.demo.DemoService;

import java.util.Date;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public class PartitionAnnotationTest {
    public static void main(String[] args) {
        DemoService demoService = (DemoService) ApplicationContextBuilder.getSpringContextBean("demoService");
        System.out.println(demoService.demoPartitionAnnotation(new Date(),new Date()));
    }

}
