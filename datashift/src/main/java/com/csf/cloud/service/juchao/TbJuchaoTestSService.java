package com.csf.cloud.service.juchao;

import com.csf.cloud.aop.annotation.Op;
import com.csf.cloud.dao.juchao.TbJuchaoTestSDao;
import com.csf.cloud.entity.juchao.TbJuchaoTestS;
import com.csf.cloud.entity.test.Dog;
import com.csf.cloud.service.Service;
import com.csf.cloud.util.JavaLogging;
import org.slf4j.Logger;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Created by soledede.weng on 2016/6/27.
 */
public class TbJuchaoTestSService extends Service<TbJuchaoTestSDao> {
    private static Logger log = JavaLogging.log();

    public void fetchJuchaoDataService(Date fromDate, Date toDate) {
        //Some Logical
        log.debug("TbJuchaoTestSService:\nBefore paritition fromDate:" + fromDate + "\ttoDate:" + toDate);
        List<TbJuchaoTestS> tbJuchaoTestS = this.getDao().fetchJuchaoData(fromDate, toDate);
        for (TbJuchaoTestS test : tbJuchaoTestS) {
            System.out.println(test.toString());
        }
        //Some Logical
    }

    public void saveDog(Dog dog) {
        this.getDao().saveDog(dog);
        System.out.println("save dog successfully");
    }


    public void updateDog(Dog dog) {
        this.getDao().updateDog(dog);
    }

    public Boolean saveOrupdateDog(Dog dog) {
        return this.getDao().saveOrupdateDog(dog);
    }

    public List<Dog> findDogs() {
        return this.getDao().findDogs();
    }

    public Dog findDog() {
        return getDao().findDog("_id", "test_id");
    }

    public List<Dog> findDogsByHeight(Double height, Op op) {
        return getDao().findDogsByHeight(100.2, Op.LTE);
    }

}
