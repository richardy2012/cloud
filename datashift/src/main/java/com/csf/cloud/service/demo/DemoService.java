package com.csf.cloud.service.demo;

import com.csf.cloud.aop.annotation.NeedPartition;
import com.csf.cloud.aop.annotation.Op;
import com.csf.cloud.service.Service;
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

    @NeedPartition(from = "from", to = "to", leftOp = Op.GTE, rightOp = Op.LTE)
    public void demoPartitionIntegerAnnotation( Integer from,Integer to) {
        System.out.println("Come in method demoPartitionAnnotation \nfrom:" + from + "\n to" + to);
    }

}
