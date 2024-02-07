package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.utils.HttpClientUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class VideoService {

    public String getVideoMyPaper() {
        JSONObject jsonObject = HttpClientUtils.httpGet("https://dayu.qqsuu.cn/moyuribaoshipin/apis.php?type=json");
        String url = jsonObject.getString("data");
        url = url.replace("https", "http");
        return url;
    }

    public String getDanceVideo(){
        JSONObject jsonObject = HttpClientUtils.httpGet("http://www.wudada.online/Api/ScSp");
        String url = jsonObject.getString("data");
        return url;
    }

}
