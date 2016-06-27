package com.chinascope.cloud.ds.mongo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoOptions;
import org.mongodb.morphia.Morphia;

import java.util.Map;

/**
 * Created by soledede.weng on 2016/6/27.
 */
public class MongoDatastore {

    /**
     * 创建一个新的实例 MongoDatastore.
     * <p>Title: </p>
     * <p>Description: </p>
     *
     * @param morphia
     * @param mongo
     * @param dbName
     */
    public MongoDatastore(Morphia morphia, Mongo mongo, String dbName) {
        morphia.createDatastore((MongoClient) mongo, dbName);
    }
}


