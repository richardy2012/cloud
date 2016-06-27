package com.chinascope.cloud.service.juchao;

import com.chinascope.cloud.dao.juchao.TbJuchaoTestSDao;
import com.chinascope.cloud.entity.juchao.TbJuchaoTestS;
import com.chinascope.cloud.service.Service;
import com.chinascope.cloud.util.JavaLogging;
import org.apache.ibatis.annotations.Param;
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
        this.getDao().fetchJuchaoData(fromDate, toDate);
        //Some Logical
    }

}
