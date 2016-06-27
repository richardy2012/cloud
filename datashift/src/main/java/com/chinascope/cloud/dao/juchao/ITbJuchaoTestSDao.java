package com.chinascope.cloud.dao.juchao;

import com.chinascope.cloud.dao.BaseDao;
import com.chinascope.cloud.entity.juchao.TbJuchaoTestS;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by soledede.weng on 2016/6/27.
 */
public interface ITbJuchaoTestSDao {

    List<TbJuchaoTestS> fetchJuchaoData(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);
}
