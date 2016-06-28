package com.csf.cloud.ds.mongo;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.DatastoreImpl;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;

/**
 * Created by soledede.weng on 2016/6/27.
 */
public class MongoDatastore extends DatastoreImpl implements Datastore {

    /**
     * @param morphia
     * @param mongoClient
     * @param dbName
     */
    public MongoDatastore(Morphia morphia, MongoClient mongoClient, String dbName) {
        super(morphia, mongoClient, dbName);
    }
}


