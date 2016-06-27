package com.aug3.storage.redisclient;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.aug3.storage.redisclient.mock.MockObj;

public class TestJedisAdaptor {

	@Test
	public void test() {

		JedisAdaptor jedis = new JedisAdaptor();

		String json = "{'emps':'10','empe':'100','sort':'','seq':'','offset':'','limit':''}";
		jedis.setex("test:exp_excel_export_1_20121127", json, 10000);

		System.out.println(jedis.get("test:exp_excel_export_1_20121127"));
		//
		// // jedis.mset("test:test_by_roger_mset", "xia_1", "roger_2",
		// "xia_2");
		// // List<String> names = jedis.mget("test:test_by_roger_mset",
		// "roger_2");
		// // for (String name : names) {
		// // System.out.println(name);
		// // }
		//
		// HashMap<String, String> m = new HashMap<String, String>();
		// m.put("name", "roger");
		// m.put("age", "30");
		// m.put("sex", "male");
		// //jedis.hmset("test:test_by_roger_hmset", m);
		//
		// // jedis.remove("test:test_by_roger_set", "test:test_by_roger_mset",
		// "test:test_by_roger_hmset");
		//
		// List l = new ArrayList();
		// TestObject obj = new TestObject();
		// obj.setName("roger");
		// l.add(obj);
		// long t1 = System.currentTimeMillis();
		// jedis.setexSerializableObj("test:test_by_roger_getobj", l, 100);
		// List<TestObject> ll = (List<TestObject>)
		// jedis.getSerializableObj("test:test_by_roger_getobj");
		// Assert.assertEquals("roger", ll.get(0).getName());
		// long t2 = System.currentTimeMillis();
		// System.out.println(t2 - t1);

		Map<String, MockObj> m = new HashMap<String, MockObj>();
		MockObj o = new MockObj();
		o.setName("Roger");
		o.setAge(31);
		m.put("roger", o);
		m.put("tim", o);

		try {
			jedis.setexSerializableObj("test:rogerTestObj", m, 1000);
			System.out.println(((Map<String, MockObj>) jedis.getSerializableObj("test:rogerTestObj")).get("roger")
					.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		jedis.hsetObj("test:rogerTestHash", "edward", o);
		jedis.expire("test:rogerTestHash", 1000);
		System.out.println(jedis.hgetObj("test:rogerTestHash", MockObj.class, "edward").getName());

		jedis.hmsetObj("test:rogerTestHash", m);
		jedis.expire("test:rogerTestHash", 1000);
		System.out.println(jedis.hmgetObj("test:rogerTestHash", MockObj.class, "roger").get("roger").getName());
	}

}
