package com.csf.cloud.entity;

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

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
