package com.ai.aigenerate.chat.custom;


import cn.hutool.json.JSONObject;
import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.chat.tool.GoogleSearchService;
import com.ai.aigenerate.model.request.baidu.SearchRequest;
import com.alibaba.fastjson2.JSON;
import com.unfbx.chatgpt.entity.chat.Functions;
import com.unfbx.chatgpt.entity.chat.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Component
public class GoogleSearchGptFunctionHandler extends AbstractGptFunctionHandler<SearchRequest> {

    @Autowired
    private GoogleSearchService googleSearchService;

    @Value("${function.google.search.desc:" +
            "当需要使用谷歌搜索实时信息或资讯有关的问题会通过意图识别决策到该插件，使用 \"搜索\" 或 \"查询\" 等关键词可以提高命中率\n" +
            "You need to fully understand user needs and provide as complete and accurate search keywords as possible. \n" +
            "Separate multiple keywords with spaces.  \n" +
            "Please avoid providing any extra text, so that I can directly pass the keywords to the search engine，搜索结果以json格式返回}")
    private String googleSearchFunctionDesc;

    @Override
    public String doHandle(String paramJson) {
        SearchRequest searchRequest = JSON.parseObject(paramJson, SearchRequest.class);
        return googleSearchService.googleSearch(searchRequest.getKeyword());
    }

    @Override
    public Functions getFunction() {
        JSONObject keyword = new JSONObject();
        keyword.putOpt("type", "string");
        keyword.putOpt("description", "查询的关键字,参数中不允许出现空格");

        //参数
        JSONObject properties = new cn.hutool.json.JSONObject();
        properties.putOpt("keyword", keyword);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("keyword")).build();
        Functions functions = Functions.builder()
                .name("googleSearch")
                .description(googleSearchFunctionDesc)
                .parameters(parameters)
                .build();
        return functions;
    }
}
