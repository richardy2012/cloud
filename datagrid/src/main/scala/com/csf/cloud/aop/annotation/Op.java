package com.csf.cloud.aop.annotation;

/**
 * Created by soledede.weng on 2016/6/24.
 */
public enum Op {
    GT(">"), LT("<"), GTE(">="), LTE("<=");

    Op(String opVal) {
        this.opVal = opVal;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    private String opVal;
}
