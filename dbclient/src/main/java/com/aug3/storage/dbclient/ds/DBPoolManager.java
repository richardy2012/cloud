package com.aug3.storage.dbclient.ds;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.aug3.storage.dbclient.Context;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

public class DBPoolManager {

	private final static Logger log = Logger.getLogger(DBPoolManager.class);

	private static ConcurrentHashMap<String, BoneCPConfig> config_map = new ConcurrentHashMap<String, BoneCPConfig>(2);
	private static ConcurrentHashMap<String, BoneCP> pool_map = new ConcurrentHashMap<String, BoneCP>(2);

	public static BoneCP initConnectionPool(Context ctx) {

		String key = ctx.toString();

		BoneCP pool = pool_map.get(key);

		if (pool == null) {
			BoneCPConfig config = config_map.get(key);
			try {
				if (config == null) {
					config = DBPoolConfig.getBoneCPConfig(ctx);
					config_map.put(key, config);
				}
				pool = new BoneCP(config);
				pool_map.put(key, pool);
			} catch (SQLException e) {
				log.error(" failed to initConnectionPool " + e.getMessage());
			}
		}

		return pool;
	}

}
