package com.chinascope.cloud.annotation;

import com.chinascope.cloud.context.ApplicationContextBuilder;
import com.chinascope.cloud.service.DemoService;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public class PartitionAnnotationTest {
    public static void main(String[] args) {
        DemoService demoService = (DemoService) ApplicationContextBuilder.getSpringContextBean("demoService");
        System.out.println(demoService.demoPartitionAnnotation());
    }

}
