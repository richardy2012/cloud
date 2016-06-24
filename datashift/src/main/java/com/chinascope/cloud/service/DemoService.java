package com.chinascope.cloud.service;

import com.chinascope.cloud.aop.annotation.NeedPartition;
import com.chinascope.cloud.aop.annotation.Op;
import org.apache.ibatis.annotations.Param;

import java.util.Date;


/**
 * Created by soledede.weng on 2016/6/23.
 */
public class DemoService extends Service {


    @NeedPartition(from = "from", to = "to", leftOp = Op.GT, rightOp = Op.LTE)
    public String demoPartitionAnnotation(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate) {
        System.out.println("Come in method demoPartitionAnnotation:partition" + fromDate);
        return "haha";
    }

}
