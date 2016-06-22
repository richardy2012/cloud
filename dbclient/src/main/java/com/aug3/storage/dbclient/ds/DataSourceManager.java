package com.aug3.storage.dbclient.ds;

import java.util.concurrent.ConcurrentHashMap;

import com.aug3.storage.dbclient.Context;
import com.jolbox.bonecp.BoneCPDataSource;

public class DataSourceManager {

	private static ConcurrentHashMap<Context, BoneCPDataSource> dsMap = new ConcurrentHashMap<Context, BoneCPDataSource>();

	public static BoneCPDataSource getDataSource(Context ctx) {

		BoneCPDataSource ds = dsMap.get(ctx);
		if (ds == null) {
			ds = new BoneCPDataSource();

			String prefix = ctx.getDbKey() == null ? "" : ctx.getDbKey() + ".";

			ds.setDriverClass(DBPoolConfig.configProperties.getProperty(prefix + "db.driverclass",
					"com.mysql.jdbc.Driver"));

			DBPoolConfig.resetPoolConfig(ds, ctx);

		}

		return ds;

	}
}
