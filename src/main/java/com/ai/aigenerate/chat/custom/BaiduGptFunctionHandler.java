package com.ai.aigenerate.chat.custom;

import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.model.request.baidu.SearchRequest;
import com.ai.aigenerate.utils.HttpClientUtils;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.unfbx.chatgpt.entity.chat.Functions;
import com.unfbx.chatgpt.entity.chat.Parameters;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class BaiduGptFunctionHandler extends AbstractGptFunctionHandler<SearchRequest> {


    @Override
    public String doHandle(String paramJson) {
        SearchRequest searchRequest = JSON.parseObject(paramJson, SearchRequest.class);
        String key = searchRequest.getKeyword().replace(" ","");
        JSONObject jsonObject = HttpClientUtils.httpGet("https://baike.baidu.com/api/openapi/BaikeLemmaCardApi?scope=103&format=json&appid=379020&bk_key="+key+"&bk_length=600");
        return jsonObject.toJSONString();
    }

    @Override
    public Functions getFunction() {
        cn.hutool.json.JSONObject keyword = new cn.hutool.json.JSONObject();
        keyword.putOpt("type", "string");
        keyword.putOpt("description", "查询的关键字,参数中不允许出现空格");

        //参数
        cn.hutool.json.JSONObject properties = new cn.hutool.json.JSONObject();
        properties.putOpt("keyword", keyword);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("keyword")).build();
        Functions functions = Functions.builder()
                .name("baiduBaikeSearch")
                .description("百度百科搜索，关键字不允许出现空格，搜索结果以json格式返回")
                .parameters(parameters)
                .build();
        return functions;
    }
}
