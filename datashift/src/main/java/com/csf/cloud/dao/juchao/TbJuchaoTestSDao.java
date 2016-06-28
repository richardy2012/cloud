package com.csf.cloud.dao.juchao;

import com.csf.cloud.aop.annotation.NeedPartition;
import com.csf.cloud.aop.annotation.Op;
import com.csf.cloud.dao.BaseDao;
import com.csf.cloud.dao.mongo.DogDao;
import com.csf.cloud.dao.mongo.MongoBaseDao;
import com.csf.cloud.entity.juchao.TbJuchaoTestS;
import com.csf.cloud.entity.test.Dog;
import com.csf.cloud.util.JavaLogging;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Created by soledede.weng on 2016/6/27.
 */
public class TbJuchaoTestSDao extends MongoBaseDao<DogDao> {
    private static Logger log = JavaLogging.log();

    @Autowired
    ITbJuchaoTestSDao iTbJuchaoTestSDao;

    @Autowired
    DogDao dogDao;




    @NeedPartition(from = "fromDate", to = "toDate", leftOp = Op.GTE, rightOp = Op.LTE)
    public List<TbJuchaoTestS> fetchJuchaoData(Date fromDate, Date toDate) {
        log.debug("TbJuchaoTestSDao\nHave Split by paritition fromDate:" + fromDate + "\ttoDate:" + toDate);
        return iTbJuchaoTestSDao.fetchJuchaoData(fromDate, toDate);
    }



    public void saveDog(Dog dog) {
        dogDao.save(dog);
    }

    public void updateDog(Dog dog) {
        dogDao.getDatastore().merge(dog);
    }

    public List<Dog> findDogs() {
        return dogDao.find().asList();
    }

    public Dog findDog(final String key, final Object value) {
        return dogDao.findOne(key, value);
    }


    public List<Dog> findDogsByHeight(Double height, Op op) {
        Query query = dogDao.getDatastore().createQuery(this.getClass());
        switch (op.ordinal()) {
            case 0:
                query.field("height").greaterThan(height);
                break;
            case 1:
                query.field("height").lessThan(height);
                break;
            case 2:
                query.field("height").greaterThanOrEq(height);
                break;
            case 3:
                query.field("height").lessThanOrEq(height);
                break;
        }
        return query.asList();
    }
}
