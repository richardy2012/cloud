package com.csf.cloud.ds.mongo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Morphia;

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


