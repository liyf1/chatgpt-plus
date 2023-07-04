package com.ai.aigenerate;

import com.ai.aigenerate.model.request.news.NewsRequest;
import com.ai.aigenerate.utils.HttpClientUtils;
import com.alibaba.fastjson.JSONObject;
import com.baidubce.http.ApiExplorerClient;
import com.baidubce.http.AppSigner;
import com.baidubce.http.HttpMethodName;
import com.baidubce.model.ApiExplorerRequest;
import com.baidubce.model.ApiExplorerResponse;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ApiTest {

    @Test
    public void weatherTest() {
        String path = "http://gwgp-n6uzuwmjrou.n.bdcloudapi.com/weather/query";
        ApiExplorerRequest request = new ApiExplorerRequest(HttpMethodName.POST, path);
        request.setCredentials("", "");

        request.addHeaderParameter("Content-Type", "application/json;charset=UTF-8");

        request.addQueryParameter("city", "上海");
        request.addQueryParameter("cityid", "");
        request.addQueryParameter("citycode", "");
        request.addQueryParameter("location", "");
        request.addQueryParameter("ip", "");

        ApiExplorerClient client = new ApiExplorerClient(new AppSigner());

        try {
            ApiExplorerResponse response = client.sendRequest(request);
            // 返回结果格式为Json字符串
            System.out.println(response.getResult());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void newsTest(){
        NewsRequest newsRequest = new NewsRequest();
        Map map = new HashMap();
        map.put("key","");
        map.put("type","top");
        map.put("page",1);
        map.put("page_size",10);
        map.put("is_filter",1);
        JSONObject jsonObject = HttpClientUtils.httpGet("http://v.juhe.cn/toutiao/index",map);
        System.out.println(jsonObject);
    }
}
