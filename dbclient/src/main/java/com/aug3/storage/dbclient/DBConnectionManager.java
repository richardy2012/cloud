package com.aug3.storage.dbclient;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.aug3.storage.dbclient.ds.DataSourceManager;
import com.jolbox.bonecp.BoneCPDataSource;

public class DBConnectionManager {

	private static final Logger log = Logger.getLogger(DBConnectionManager.class);

	private BoneCPDataSource ds;

	public DBConnectionManager(Context ctx) {
		ds = DataSourceManager.getDataSource(ctx);
	}

	public Connection getConnection() {

		if (ds != null) {
			try {
				Connection connection = ds.getConnection();
				return connection;
			} catch (SQLException e) {
				log.info("error occured while try to get db connection!");
			}
		} else {
			log.warn("DB connection pool is not correctly initialized!");
		}
		return null;

	}

	public void closeConnection(Connection connection) {
		try {
			connection.close();
		} catch (SQLException e) {
			connection = null;
			log.error(" failed to release connection " + e.getMessage());
		}
	}

	public static void releaseResource(Statement statement, Connection connection) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				statement = null;
			}
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				connection = null;
			}
		}
	}

}
