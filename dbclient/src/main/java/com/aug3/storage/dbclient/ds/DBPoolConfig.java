package com.aug3.storage.dbclient.ds;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.aug3.storage.dbclient.Context;
import com.jolbox.bonecp.BoneCPConfig;

public class DBPoolConfig {

    public final static Properties configProperties = new LazyPropLoader("/common-db.properties");

    public static BoneCPConfig getBoneCPConfig(Context ctx) {

        BoneCPConfig config = new BoneCPConfig();

        resetPoolConfig(config, ctx);

        return config;

    }

    static void resetPoolConfig(BoneCPConfig config, Context ctx) {

        // juchao.db.jdbcurl
        // juchao.db.username
        // reuters.db.jdbcurl
        // reuters.db.username
        // reuters.db.password
        String prefix = ctx.getDbKey() == null ? "" : ctx.getDbKey() + ".";

        config.setJdbcUrl(configProperties.getProperty(prefix + "db.jdbcurl"));
        config.setUsername(configProperties.getProperty(prefix + "db.username"));
        config.setPassword(configProperties.getProperty(prefix + "db.password"));

        /**
         * In order to reduce lock contention and thus improve performance, each
         * incoming connection request picks off a connection from a pool that
         * has thread-affinity, i.e. pool[threadId % partition_count]. The
         * higher this number, the better your performance will be for the case
         * when you have plenty of short-lived threads. Beyond a certain
         * threshold, maintenence of these pools will start to have a negative
         * effect on performance (and only for the case when connections on a
         * partition start running out).
         */
        config.setPartitionCount(Integer.valueOf(configProperties.getProperty(prefix + "jdbc.partitionCount", "2")));

        /**
         * The number of connections to create per partition. Setting this to 5
         * with 3 partitions means you will have 15 unique connections to the
         * database. Note that BoneCP will not create all these connections in
         * one go but rather start off with minConnectionsPerPartition and
         * gradually increase connections as required.
         */
        config.setMaxConnectionsPerPartition(Integer.valueOf(configProperties.getProperty(prefix
                + "jdbc.maxConnectionsPerPartition", "5")));

        config.setMinConnectionsPerPartition(Integer.valueOf(configProperties.getProperty(prefix
                + "jdbc.minConnectionsPerPartition", "2")));

        /**
         * When the available connections are about to run out, BoneCP will
         * dynamically create new ones in batches. This property controls how
         * many new connections to create in one go (up to a maximum of
         * maxConnectionsPerPartition). Note: This is a per partition setting.
         * 
         * Default: 10
         */
        config.setAcquireIncrement(Integer.valueOf(configProperties.getProperty(prefix + "jdbc.acquireIncrement", "5")));
        
        /**
         * statementsCacheSize - Statement实例缓存个数 默认值：100
         */
        config.setStatementsCacheSize(Integer.valueOf(configProperties.getProperty(prefix
                + "jdbc.statementsCacheSize", "100")));
        
        /**
         * releaseHelperThreads - 每个分区释放链接助理进程的数量 默认值：3
         */
        config.setReleaseHelperThreads(Integer.valueOf(configProperties.getProperty(prefix
                + "jdbc.releaseHelperThreads", "3")));
        
        
        /**
         * defaultAutoCommit
         */
        config.setDefaultAutoCommit(Boolean.valueOf(configProperties.getProperty(prefix
                + "jdbc.defaultAutoCommit", "true")));
        
        /**
         * connectionTimeout - 设置获取connection超时的时间(分)。 默认：10
         */
        config.setConnectionTimeout(
                Long.valueOf(configProperties.getProperty(prefix + "jdbc.connectionTimeout",
                        Integer.toString(10))), TimeUnit.MINUTES);

        /**
         * idleConnectionTestPeriodInMinutes - 检查数据库连接池中空闲连接的间隔时间(分) 默认：60
         */
        config.setIdleConnectionTestPeriod(
        		Long.valueOf(configProperties.getProperty(prefix + "jdbc.idleConnectionTestPeriod",
                        Integer.toString(60))), TimeUnit.MINUTES);
                        
        /**
         * idleMaxAgeInMinutes - 连接池中未使用的链接最大存活时间(分) 默认：240
         */
        config.setIdleMaxAge(
        		Long.valueOf(configProperties.getProperty(prefix + "jdbc.idleMaxAge",
                        Integer.toString(240))), TimeUnit.MINUTES);
        
        /**
         * maxConnectionAge - connection的存活时间(分) 默认：240
         */
        config.setMaxConnectionAge(Long.valueOf(configProperties.getProperty(prefix + "jdbc.maxConnectionAge",
                        Integer.toString(240))), TimeUnit.MINUTES);
        
    }
}
