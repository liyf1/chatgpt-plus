package com.ai.aigenerate.chat.custom;


import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.chat.tool.WeiboService;
import com.ai.aigenerate.model.request.weibo.WeiboRequest;
import com.alibaba.fastjson2.JSON;
import com.unfbx.chatgpt.entity.chat.Functions;
import com.unfbx.chatgpt.entity.chat.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class WeiboGptFunctionHandler extends AbstractGptFunctionHandler<WeiboRequest> {

    @Autowired
    private WeiboService weiboService;

    @Override
    public String doHandle(String paramJson) {
        WeiboRequest weiboRequest = JSON.parseObject(paramJson, WeiboRequest.class);
        String result = weiboService.getWeiboResult(weiboRequest.getType());
        return result;
    }

    @Override
    public Functions getFunction() {
        cn.hutool.json.JSONObject type = new cn.hutool.json.JSONObject();
        type.putOpt("type", "string");
        type.putOpt("description", "热榜的类型，可选值：hotSearch(实时热搜榜)、topic(话题榜)、importantNews(要闻榜)、movie(电影榜)、entertainment(文娱榜)");
        type.putOpt("enum",Arrays.asList("hotSearch","topic","importantNews","movie","entertainment"));

        //参数
        cn.hutool.json.JSONObject properties = new cn.hutool.json.JSONObject();
        properties.putOpt("type", type);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("num")).build();
        Functions functions = Functions.builder()
                .name("weiboHotSearch")
                .description("获取微博热搜数据，必须提及微博热搜才进行调用")
                .parameters(parameters)
                .build();
        return functions;
    }
}
