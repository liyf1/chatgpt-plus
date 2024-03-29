package com.ai.aigenerate.chat.custom;


import cn.hutool.json.JSONObject;
import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.chat.tool.BaiduSearchService;
import com.ai.aigenerate.model.request.baidu.SearchRequest;
import com.alibaba.fastjson2.JSON;
import com.unfbx.chatgpt.entity.chat.Functions;
import com.unfbx.chatgpt.entity.chat.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class BaiduSearchGptFunctionHandler extends AbstractGptFunctionHandler<SearchRequest> {

    @Autowired
    private BaiduSearchService baiduSearchService;

    @Value("${function.baidu.search.desc:通过百度进行搜索,搜索结果以json格式返回}")
    private String baiduSearchFunctionDesc;

    @Override
    public String doHandle(String paramJson) {
        SearchRequest searchRequest = JSON.parseObject(paramJson, SearchRequest.class);
        return baiduSearchService.getBaiduSearchResult(searchRequest.getKeyword());
    }

    @Override
    public Functions getFunction() {
        JSONObject keyword = new JSONObject();
        keyword.putOpt("type", "string");
        keyword.putOpt("description", "查询的关键字,参数中不允许出现空格");

        //参数
        JSONObject properties = new JSONObject();
        properties.putOpt("keyword", keyword);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("keyword")).build();
        Functions functions = Functions.builder()
                .name("baiduSearch")
                .description(baiduSearchFunctionDesc)
                .parameters(parameters)
                .build();
        return functions;
    }
}
