package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.utils.HttpClientUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class MorningPaperService {

    public String getMorningPaper() {
        JSONObject jsonObject = HttpClientUtils.httpGet("http://dwz.2xb.cn/zaob");
        String url = jsonObject.getString("imageUrl");
        url = url.replace("https", "http");
        return url;
    }
}
