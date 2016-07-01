package com.csf.cloud.aop.annotation;

import java.lang.annotation.*;

/**
 * Created by soledede.weng on 2016/6/23.
 */
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface NeedPartition {
    String value() default "none";

    String from();

    String to();

    Op leftOp();

    Op rightOp();

    String comments() default "none";
}


