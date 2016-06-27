package com.chinascope.cloud.service.demo;

import com.chinascope.cloud.aop.annotation.NeedPartition;
import com.chinascope.cloud.aop.annotation.Op;
import com.chinascope.cloud.service.Service;
import org.apache.ibatis.annotations.Param;

import java.util.Date;


/**
 * Created by soledede.weng on 2016/6/23.
 */
public class DemoService extends Service<DemoService> {


    @NeedPartition(from = "fromDate", to = "toDate", leftOp = Op.GTE, rightOp = Op.LTE)
    public String demoPartitionAnnotation(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate) {
        System.out.println("Come in method demoPartitionAnnotation \nfromDate:" + fromDate + "\n toDateL" + toDate);
        return "haha";
    }

}
