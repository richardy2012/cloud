package com.aug3.storage.redisclient;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.aug3.storage.redisclient.mock.MockObj;
import com.google.gson.reflect.TypeToken;

public class TestJedisClusterAdaptor {

	@Test
	public void test() {

		JedisClusterAdaptor adpt = JedisClusterAdaptor.getInstance();

		adpt.set("test:name", "roger");

		System.out.println(adpt.get("test:name"));

		adpt.del("test:name");

		Map<String, MockObj> m = new HashMap<String, MockObj>();
		MockObj o = new MockObj();
		o.setName("RogerXia");
		o.setAge(31);
		m.put("roger", o);
		m.put("tim", o);

		adpt.setexObject("test:testObj1", m, 5000);
		System.out.println(((Map<String, MockObj>) adpt.getObject("test:testObj1",
				new TypeToken<Map<String, MockObj>>() {
				}.getType())).get("roger").getName());

		try {
			adpt.setexSerializableObj("test:testObj2", m, 5000);

			System.out
					.println(((Map<String, MockObj>) adpt.getSerializableObj("test:testObj2")).get("roger").getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
