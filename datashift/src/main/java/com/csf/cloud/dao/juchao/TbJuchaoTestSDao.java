package com.csf.cloud.dao.juchao;

import com.csf.cloud.aop.annotation.NeedPartition;
import com.csf.cloud.aop.annotation.Op;
import com.csf.cloud.dao.BaseDao;
import com.csf.cloud.dao.mongo.DogDao;
import com.csf.cloud.entity.juchao.TbJuchaoTestS;
import com.csf.cloud.entity.test.Dog;
import com.csf.cloud.util.JavaLogging;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Created by soledede.weng on 2016/6/27.
 */
public class TbJuchaoTestSDao extends BaseDao {
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

}
