package com.ai.aigenerate.chat.custom;


import cn.hutool.json.JSONObject;
import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.chat.tool.BaiduSearchService;
import com.ai.aigenerate.model.request.baidu.BaiduSearchRequest;
import com.alibaba.fastjson2.JSON;
import com.unfbx.chatgpt.entity.chat.Functions;
import com.unfbx.chatgpt.entity.chat.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Arrays;

@Component
public class BaiduSearchGptFunctionHandler extends AbstractGptFunctionHandler<BaiduSearchRequest> {

    @Autowired
    private BaiduSearchService baiduSearchService;

    @Override
    public String doHandle(String paramJson) {
        BaiduSearchRequest baiduSearchRequest = JSON.parseObject(paramJson, BaiduSearchRequest.class);
        return baiduSearchService.getBaiduSearchResult(baiduSearchRequest.getKeyword());
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
                .name("baiduSearch")
                .description("通过百度进行搜索，关键字不允许出现空格，搜索结果以json格式返回")
                .parameters(parameters)
                .build();
        return functions;
    }
}
