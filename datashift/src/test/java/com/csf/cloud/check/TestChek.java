package com.csf.cloud.check;

import com.csf.cloud.bloomfilter.CanGenerateHashFrom;
import com.csf.cloud.bloomfilter.mutable.BloomFilter;
import com.csf.cloud.config.JavaConfiguration;
import com.csf.cloud.storage.Storage;
import com.csf.cloud.storage.Storage$;
import com.csf.cloud.util.BizConstant;

/**
 * Created by soledede.weng on 2016/6/29.
 */
public class TestChek {

    static BloomFilter<String> bloomFilter = BloomFilter.apply("unqiue_primary_key", JavaConfiguration.expectedElements(),
            JavaConfiguration.falsePositiveRate(),
            CanGenerateHashFrom.CanGenerateHashFromString$.MODULE$);
    static Storage storage = Storage$.MODULE$.apply("redis");

    public static void main(String[] args) {
        addPrimaryKeyToRedis();
    }

    public static void addPrimaryKeyToRedis() {
        storage.setStringByKey(BizConstant.CHECK_PREFFIX() + "57724f06d62b8a19e8e0e66c", "height|hair");
        System.out.println(storage.getStringBykey(BizConstant.CHECK_PREFFIX() + "57724f06d62b8a19e8e0e66c"));
    }

}
