package com.aug3.storage.dbclient;

import java.sql.Connection;

import org.junit.Test;

public class TestDBConnectionManager {
	@Test
	public void test() {
		Context ctx = new Context();
		ctx.setDbKey("");
		ctx.setDbType("oracle");
		DBConnectionManager db = new DBConnectionManager(ctx);
		Connection oraclecon = db.getConnection();
		System.out.println(oraclecon);

		ctx.setDbType("mysql");
		Connection mysqlcon = db.getConnection();
		System.out.println(mysqlcon);

	}
}
