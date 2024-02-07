package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.utils.HttpClientUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class CountDownHourService {

    public String queryWord(){
        JSONObject jsonObject = HttpClientUtils.httpGet("https://zj.v.api.aa1.cn/api/wenan-mj/?type=json");
        String msg = jsonObject.getString("msg");
        return msg;
    }

    public String countDownHour(){
        JSONObject jsonObject = HttpClientUtils.httpGet("http://v.api.aa1.cn/api/rsdjs/");
        String month = jsonObject.getString("month");
        String week = jsonObject.getString("week");
        String day = jsonObject.getString("day");
        String time = jsonObject.getString("time");
        String str = "\n**人生倒计时：**\n" +
                "\n" +
                "- "+month+"\n" +
                "- "+week+"\n" +
                "- "+day+"\n" +
                "- "+time+"";
        return str;
    }
}
