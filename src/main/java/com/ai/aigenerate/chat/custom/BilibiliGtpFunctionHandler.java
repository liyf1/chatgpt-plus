package com.ai.aigenerate.chat.custom;

import cn.hutool.json.JSONObject;
import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.chat.tool.BilibiliService;
import com.ai.aigenerate.model.request.Bilibili.BilibiliRequest;
import com.ai.aigenerate.model.response.bilibili.BilibiliResponse;
import com.alibaba.fastjson2.JSON;
import com.unfbx.chatgpt.entity.chat.Functions;
import com.unfbx.chatgpt.entity.chat.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class BilibiliGtpFunctionHandler extends AbstractGptFunctionHandler<BilibiliRequest> {

    @Autowired
    private BilibiliService bilibiliService;

    @Override
    public String doHandle(String paramJson) {
        BilibiliRequest bilibiliRequest = JSON.parseObject(paramJson, BilibiliRequest.class);
        BilibiliResponse response = bilibiliService.getBilibiliVideo(bilibiliRequest);
        String content = "{ " +
                "\"视频的名称\": \""+response.getTitle()+"\"" +
                "\"视频的介绍\": \""+response.getDesc()+"\"" +
                "\"视频的up主\": \""+response.getUpName()+"\"" +
                "\"视频的详细信息\": \""+response.getDetail()+"\"" +
                "}";
        return content;
    }

    @Override
    public Functions getFunction() {
        JSONObject videoUrl = new JSONObject();
        videoUrl.putOpt("type", "string");
        videoUrl.putOpt("description", "视频的url");
        //参数
        JSONObject properties = new JSONObject();
        properties.putOpt("videoUrl", videoUrl);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("videoUrl")).build();
        Functions functions = Functions.builder()
                .name("getBilibiliVideoInfo")
                .description("获取bilibili视频信息")
                .parameters(parameters)
                .build();
        return functions;
    }
}
