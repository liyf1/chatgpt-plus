package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.config.JuheKey;
import com.ai.aigenerate.model.request.news.NewsRequest;
import com.ai.aigenerate.utils.HttpClientUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NewsService {

    @Autowired
    private JuheKey juheKey;

    public String queryNews(NewsRequest newsRequest) {
        Map<String,Object> map = new HashMap();
        map.put("key",juheKey.getNewsKey());
        map.put("type",newsRequest.getType());
        map.put("page",newsRequest.getPage());
        map.put("page_size",newsRequest.getPageSize());
        map.put("is_filter",newsRequest.getIsFilter());
        JSONObject jsonObject = HttpClientUtils.httpGet("http://v.juhe.cn/toutiao/index",map);
        return jsonObject.toJSONString();
    }
}
