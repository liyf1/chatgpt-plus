package com.ai.aigenerate.chat.custom;

import cn.hutool.json.JSONObject;
import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.model.request.time.TimeRequest;
import com.alibaba.fastjson2.JSON;
import com.unfbx.chatgpt.entity.chat.Functions;
import com.unfbx.chatgpt.entity.chat.Parameters;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Component
public class CurrentTimeGptFunctionHandler extends AbstractGptFunctionHandler<TimeRequest> {

    @Override
    public String doHandle(String paramJson) {
        TimeRequest timeRequest = JSON.parseObject(paramJson, TimeRequest.class);
        ZoneId zoneId = ZoneId.of(timeRequest.getTimeZone());
        ZonedDateTime currentTimeDefault = ZonedDateTime.now(zoneId);
        // 获取毫秒级的时间戳
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 格式化时间为指定格式的字符串
        String formattedDateTime = formatter.format(currentTimeDefault);
        String content
                = "{ " +
                "\"获取到的时间\": \""+formattedDateTime+"\"" +
                "}";
        return content;
    }

    @Override
    public Functions getFunction() {
        JSONObject zone = new JSONObject();
        zone.putOpt("type", "string");
        zone.putOpt("description", "时区信息，比如：Asia/Shanghai、America/New_York、UTC、GMT+8");
        //参数
        JSONObject properties = new JSONObject();
        properties.putOpt("timeZone", zone);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("timeZone")).build();
        Functions functions = Functions.builder()
                .name("getCurrentTime")
                .description("根据时区获取当前时间")
                .parameters(parameters)
                .build();
        return functions;
    }
}
