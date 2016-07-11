package com.csf.cloud.excute.juchao;

import com.csf.cloud.bloomfilter.CanGenerateHashFrom;
import com.csf.cloud.bloomfilter.mutable.BloomFilter;
import com.csf.cloud.config.JavaConfiguration;
import com.csf.cloud.entity.test.Dog;
import com.csf.cloud.entity.test.Finger;
import com.csf.cloud.excute.DefaultExcutor;
import com.csf.cloud.service.juchao.TbJuchaoTestSService;
import com.csf.cloud.util.BizException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by soledede.weng on 2016/6/27.
 */
public class TbJuchaoTestSLogical extends DefaultExcutor<TbJuchaoTestSService> {

    //just for test
    static BloomFilter<String> bloomFilter = BloomFilter.apply("unqiue_primary_key", JavaConfiguration.expectedElements(),
            JavaConfiguration.falsePositiveRate(),
            CanGenerateHashFrom.CanGenerateHashFromString$.MODULE$);

    static {
        bloomFilter.add("test_id");
        if (bloomFilter.mightContain("test_id")) {
            System.out.println("true");
        } else System.out.println("false");

    }


    @Override
    public void service() throws Exception {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.set(Calendar.HOUR, c.get(Calendar.HOUR) - 10);
            Date date1 = c.getTime();
            Date currentDate = new Date();
            System.out.println("last time:" + date1 + "\t current Time" + currentDate);

            System.out.println("excutor service...current Thread:" + Thread.currentThread().getId() + "excutor:" + this.job().getPartition().getPartitionNum());
            //test for oracle mybatis
            this.bizService().fetchJuchaoDataService(date1, currentDate);
            //test for mongo
            List<Finger> figers = new ArrayList<Finger>();
            for (int i = 0; i < 10; i++) {
                figers.add(new Finger("finger" + i + 1));
            }
       /* for (int i = 0; i < 3; i++) {
            this.bizService().saveDog(new Dog("zhuangzhuang" + i, "white", 14.4 + i, figers));
        }*/
            Dog saveDog = new Dog("zhuangzhuang", "white", 40.3, figers);
            saveDog.setId("test_id");
            this.bizService().saveDog(saveDog);

            List<Dog> dogs = this.bizService().findDogs();
            for (Dog dog : dogs) {
                System.out.println(dog.toString());
            }

            //test update
            Dog dog = this.bizService().findDog();
            System.out.println("Before updated:dog:" + dog.toString());
            if (dog != null) {
                Dog dogNew = new Dog();
                dogNew.setId(dog.getId());
                dogNew.setName("bad guy");
                this.bizService().updateDog(dogNew);
                dog = this.bizService().findDog();
                System.out.println("After updated:dog:" + dog.toString());
            }
            //test query by contition


            //test save or update check unique

            Dog dogUnique = new Dog();
            dogUnique.setId(dog.getId());
            dogUnique.setName("check unique dog");
            dogUnique.setHair("long yellow");
            dogUnique.setHeight(1.2);
            this.bizService().saveOrupdateDog(dogUnique);
        } catch (Exception e) {
            throw new BizException("framework will get this exception and report it to zk,so remember do this like me");
        }
    }
}
