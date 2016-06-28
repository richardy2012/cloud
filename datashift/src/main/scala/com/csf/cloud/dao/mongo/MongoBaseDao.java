package com.csf.cloud.dao.mongo;

import com.csf.cloud.dao.BaseDao;
import org.mongodb.morphia.Datastore;

/**
 * Created by soledede.weng on 2016/6/28.
 */
public abstract class MongoBaseDao<T> extends BaseDao{

    protected void saveOrUpdate(T entity, Datastore ds){

    }
}
