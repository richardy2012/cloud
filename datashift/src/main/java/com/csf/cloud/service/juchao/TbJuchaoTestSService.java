package com.csf.cloud.service.juchao;

import com.csf.cloud.dao.juchao.TbJuchaoTestSDao;
import com.csf.cloud.entity.juchao.TbJuchaoTestS;
import com.csf.cloud.service.Service;
import com.csf.cloud.util.JavaLogging;
import org.slf4j.Logger;

import java.util.Date;
import java.util.List;

/**
 * Created by soledede.weng on 2016/6/27.
 */
public class TbJuchaoTestSService extends Service<TbJuchaoTestSDao> {
    private static Logger log = JavaLogging.log();


    public void fetchJuchaoDataService(Date fromDate, Date toDate) {
        //Some Logical
        log.debug("TbJuchaoTestSService:\nBefore paritition fromDate:"+fromDate+"\ttoDate:"+toDate);
        List<TbJuchaoTestS> tbJuchaoTestS = this.getDao().fetchJuchaoData(fromDate, toDate);
        for (TbJuchaoTestS test : tbJuchaoTestS) {
            System.out.println(test.toString());
        }
        //Some Logical
    }

}
