package com.chinascope.cloud.aop.annotation;

/**
 * Created by soledede.weng on 2016/6/23.
 */
public @interface NeedPartition {
    String value() default "none";
}
