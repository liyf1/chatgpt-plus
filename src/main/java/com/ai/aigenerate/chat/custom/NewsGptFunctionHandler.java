package com.ai.aigenerate.chat.custom;

import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.model.request.news.NewsRequest;
import com.ai.aigenerate.chat.tool.NewsService;
import com.alibaba.fastjson2.JSON;
import com.unfbx.chatgpt.entity.chat.Functions;
import com.unfbx.chatgpt.entity.chat.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class NewsGptFunctionHandler extends AbstractGptFunctionHandler<NewsRequest> {

    @Autowired
    private NewsService newsService;

    @Override
    public String doHandle(String paramJson) {
        NewsRequest newsRequest = JSON.parseObject(paramJson, NewsRequest.class);
        return newsService.queryNews(newsRequest);
    }

    @Override
    public Functions getFunction() {
        cn.hutool.json.JSONObject newsType = new cn.hutool.json.JSONObject();
        newsType.putOpt("type", "string");
        newsType.putOpt("enum",Arrays.asList("top","guonei","guoji","yule","tiyu","junshi","keji","caijing","youxi","qiche","jiankang"));
        newsType.putOpt("description", "新闻类型, 默认top");
        cn.hutool.json.JSONObject page = new cn.hutool.json.JSONObject();
        page.putOpt("type", "integer");
        page.putOpt("description", "当前页数, 默认1, 最大50");
        cn.hutool.json.JSONObject size = new cn.hutool.json.JSONObject();
        size.putOpt("type", "integer");
        size.putOpt("description", "每页返回条数, 默认30 , 最大30");
        //参数
        cn.hutool.json.JSONObject properties = new cn.hutool.json.JSONObject();
        properties.putOpt("type", newsType);
        properties.putOpt("page", page);
        properties.putOpt("size", size);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties).required(Arrays.asList("type")).build();
        Functions functions = Functions.builder()
                .name("getNews")
                .description("获取新闻信息")
                .parameters(parameters)
                .build();
        return functions;
    }
}
