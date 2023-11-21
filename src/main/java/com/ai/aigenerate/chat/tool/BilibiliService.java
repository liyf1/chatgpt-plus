package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.model.request.Bilibili.BilibiliRequest;
import com.ai.aigenerate.model.response.bilibili.BilibiliResponse;
import com.ai.aigenerate.utils.HttpClientUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class BilibiliService {

    public BilibiliResponse getBilibiliVideo(BilibiliRequest bilibiliRequest) {
        JSONObject jsonObject = HttpClientUtils.httpPost("http://localhost:5000/parseVideo", JSON.toJSONString(bilibiliRequest));
        return JSON.parseObject(jsonObject.toJSONString(), BilibiliResponse.class);
    }
}
