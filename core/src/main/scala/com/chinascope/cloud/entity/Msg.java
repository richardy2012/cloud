package com.chinascope.cloud.entity;

/**
 * Created by soledede.weng on 2016/6/12.
 */
public class Msg {
    private Integer code;
    private String message;
    private Object data;

    public Msg() {
    }

    public Msg(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Msg(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
