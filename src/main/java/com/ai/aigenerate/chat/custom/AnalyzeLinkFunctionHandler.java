package com.ai.aigenerate.chat.custom;

import cn.hutool.json.JSONObject;
import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.chat.tool.AnalyzeLinkService;
import com.ai.aigenerate.model.request.link.LinkRequest;
import com.alibaba.fastjson2.JSON;
import com.unfbx.chatgpt.entity.chat.Functions;
import com.unfbx.chatgpt.entity.chat.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class AnalyzeLinkFunctionHandler extends AbstractGptFunctionHandler<LinkRequest> {

    @Autowired
    private AnalyzeLinkService analyzeLinkService;

    @Override
    public String doHandle(String paramJson) {
        LinkRequest linkRequest = JSON.parseObject(paramJson, LinkRequest.class);
        return analyzeLinkService.analyzeLink(linkRequest.getUrl());
    }

    @Override
    public Functions getFunction() {
        JSONObject url = new JSONObject();
        url.putOpt("type", "string");
        url.putOpt("description", "链接的url，要读取的完整的链接");
        //参数
        JSONObject properties = new JSONObject();
        properties.putOpt("url", url);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("url")).build();
        Functions functions = Functions.builder()
                .name("analyzeLink")
                .description("根据给出的网址链接地址解析网页中的内容，以进行后续的分析")
                .parameters(parameters)
                .build();
        return functions;
    }
}
