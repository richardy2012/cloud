package com.csf.cloud;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.csf.cloud.entity.Msg;
import com.csf.cloud.entity.StreamDataTest;
import com.csf.cloud.util.http.HttpClientUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by soledede.weng on 2016/6/15.
 */
public class HttpClientTest {

    public static void main(String[] args) {
        testTriggerRestService();

    }


    public static void testTriggerRestService() {
        String url = "http://192.168.1.72:9898/rest/trigger/";

        StreamDataTest obj = new StreamDataTest();
        obj.setJobName("job3");
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            keys.add("key" + i);
        }
        obj.setData(keys);
        Map headers = new java.util.HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        CloseableHttpResponse httpResp = HttpClientUtil.requestHttpSyn(url, "post", obj, headers);
        try {
            HttpEntity entity = httpResp.getEntity();
            String sResponse = EntityUtils.toString(entity);
            System.out.println(sResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            HttpClientUtils.closeQuietly(httpResp);
        }
    }
}
