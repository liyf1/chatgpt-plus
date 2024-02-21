package com.ai.aigenerate.chat.custom;

import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.chat.tool.MoyuService;
import com.unfbx.chatgpt.entity.chat.Functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MoyuPaperFunctionHandler extends AbstractGptFunctionHandler<Object> {

    @Autowired
    private MoyuService moyuService;

    @Override
    public String doHandle(String paramJson) {
        return "{\"图片的png链接\":\"" + moyuService.getRelaxPaper() + "\"}";
    }

    @Override
    public Functions getFunction() {
        Functions functions = Functions.builder()
                .name("getMoyuPaper")
                .description("获取摸鱼日报的图片")
                .build();
        return functions;
    }
}