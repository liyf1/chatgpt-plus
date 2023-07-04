package com.ai.aigenerate.chat.custom;

import cn.hutool.json.JSONObject;
import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.model.request.weather.WeatherRequest;
import com.ai.aigenerate.chat.tool.WeatherService;
import com.alibaba.fastjson2.JSON;
import com.unfbx.chatgpt.entity.chat.Functions;
import com.unfbx.chatgpt.entity.chat.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class WeatherGptFunctionHandler extends AbstractGptFunctionHandler<WeatherRequest> {

    @Autowired
    private WeatherService weatherService;

    @Override
    public String doHandle(String paramJson) {
        WeatherRequest createTaskRequest = JSON.parseObject(paramJson, WeatherRequest.class);
        String result = weatherService.getWeather(createTaskRequest.getCity());
        return result;
    }

    @Override
    public Functions getFunction() {
        JSONObject city = new JSONObject();
        city.putOpt("type", "string");
        city.putOpt("description", "城市名称,例如：北京，必须是中文");
        //参数
        JSONObject properties = new JSONObject();
        properties.putOpt("city", city);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("city")).build();
        Functions functions = Functions.builder()
                .name("queryWeather")
                .description("获取天气信息，返回结果是json格式的数据，请转换为通俗易懂的结果返回")
                .parameters(parameters)
                .build();
        return functions;
    }
}
