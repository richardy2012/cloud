package com.csf.cloud.dao.mongo;

import com.csf.cloud.entity.test.Dog;
import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.dao.BasicDAO;

import java.util.List;

/**
 * Created by soledede.weng on 2016/6/28.
 */
public class DogDao extends BasicDAO<Dog, String> {


    public DogDao(MongoClient mongoClient, Morphia morphia, String dbName) {
        super(mongoClient, morphia, dbName);
    }

    public DogDao(Datastore ds) {
        super(ds);
    }




}
