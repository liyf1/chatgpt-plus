package com.ai.aigenerate.chat.custom;

import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.chat.tool.MorningPaperService;
import com.unfbx.chatgpt.entity.chat.Functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewsImageFunctionHandler extends AbstractGptFunctionHandler<Object> {

    @Autowired
    private MorningPaperService morningPaperService;

    @Override
    public String doHandle(String paramJson) {
        return "{\"图片的链接\":\"" + morningPaperService.getMorningPaper() + "\"}";
    }

    @Override
    public Functions getFunction() {
        Functions functions = Functions.builder()
                .name("getNewsPicture")
                .description("获取新闻早报的图片")
                .build();
        return functions;
    }
}