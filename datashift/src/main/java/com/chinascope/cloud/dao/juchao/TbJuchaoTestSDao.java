package com.chinascope.cloud.dao.juchao;

import com.chinascope.cloud.aop.annotation.NeedPartition;
import com.chinascope.cloud.aop.annotation.Op;
import com.chinascope.cloud.dao.BaseDao;
import com.chinascope.cloud.entity.juchao.TbJuchaoTestS;
import com.chinascope.cloud.util.JavaLogging;
import org.apache.ibatis.annotations.Param;
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


    @NeedPartition(from = "fromDate", to = "toDate", leftOp = Op.GTE, rightOp = Op.LTE)
    public List<TbJuchaoTestS> fetchJuchaoData(Date fromDate, Date toDate) {
        log.debug("TbJuchaoTestSDao\nHave Split by paritition fromDate:" + fromDate + "\ttoDate:" + toDate);
        return iTbJuchaoTestSDao.fetchJuchaoData(fromDate, toDate);
    }
}
