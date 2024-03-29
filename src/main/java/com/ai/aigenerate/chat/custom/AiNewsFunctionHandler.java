package com.ai.aigenerate.chat.custom;

import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.chat.tool.CrawlerAiNewsService;
import com.unfbx.chatgpt.entity.chat.Functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AiNewsFunctionHandler extends AbstractGptFunctionHandler<Object> {

    @Autowired
    private CrawlerAiNewsService crawlerAiNewsService;

    @Override
    public String doHandle(String paramJson) {
        return crawlerAiNewsService.getAiNews();
    }

    @Override
    public Functions getFunction() {
        Functions functions = Functions.builder()
                .name("getAiNews")
                .description("获取当天人工智能技术的资讯信息")
                .build();
        return functions;
    }
}
