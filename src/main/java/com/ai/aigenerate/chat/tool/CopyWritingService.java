package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.utils.HttpClientUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class CopyWritingService {

    public String getKfcText(){
        JSONObject jsonObject = HttpClientUtils.httpGet("https://api.khkj6.com/kfc/");
        String text = jsonObject.getString("msg");
        return text;
    }
}
