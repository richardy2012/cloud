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
        bloomFilter.add("height");
        bloomFilter.add("hair");
        if(bloomFilter.mightContain("hair")) {
            System.out.println("true");
        }else System.out.println("false");


        bloomFilter.dispose();
    }

    public static void addPrimaryKeyToRedis() {
        storage.setStringByKey(BizConstant.CHECK_PREFFIX() + "test_id", "height|hair");
        System.out.println(storage.getStringBykey(BizConstant.CHECK_PREFFIX() + "test_id"));
    }

}
