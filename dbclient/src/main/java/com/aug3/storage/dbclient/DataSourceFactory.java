package com.aug3.storage.dbclient;

import com.aug3.storage.dbclient.ds.DataSourceManager;
import com.jolbox.bonecp.BoneCPDataSource;

/**
 * Datasource 工厂, 提供单独的DataSource方便使用
 * @author falcon.chu
 *
 */
public class DataSourceFactory {
	
	public BoneCPDataSource getDataSource(Context ctx){
		return DataSourceManager.getDataSource(ctx);
	}

}
