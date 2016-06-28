package com.csf.cloud.resource;

/**
 * Created by soledede.weng on 2016/6/14.
 */
public class Bytes {
    public static String substring(String src, int start_idx, int end_idx) {
        byte[] b = src.getBytes();
        String tgt = "";
        for (int i = start_idx; i <= end_idx; i++) {
            tgt += (char) b[i];
        }
        return tgt;
    }
}
