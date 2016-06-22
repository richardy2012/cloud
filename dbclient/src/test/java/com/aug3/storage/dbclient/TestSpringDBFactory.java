package com.aug3.storage.dbclient;

import org.junit.Test;

public class TestSpringDBFactory {
	@Test
	public void test() {
		Context ctx = new Context();
		ctx.setDbKey("");
		ctx.setDbType("oracle");
		SpringDBFactory factory = new SpringDBFactory();
		System.out.println(factory.getJDBCTempldate(ctx).getDataSource());
		ctx.setDbType("mysql");
		factory = new SpringDBFactory();
		System.out.println(factory.getJDBCTempldate(ctx).getDataSource());
	}

}
