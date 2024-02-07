package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.utils.HttpClientUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class MoyuService {

    //https://api.j4u.ink/v1/store/other/proxy/remote/moyu.json

    public String getRelaxPaper() {
        //https://api.52vmy.cn/api/wl/moyu
//        JSONObject jsonObject = HttpClientUtils.httpGet("https://api.j4u.ink/v1/store/other/proxy/remote/moyu.json");
//        JSONObject data = jsonObject.getJSONObject("data");
//        String url = data.getString("moyu_url");
//        url = url.replace("https", "http");
        return "https://api.52vmy.cn/api/wl/moyu";
    }

    public String getImage(){
        JSONObject jsonObject = HttpClientUtils.httpGet("https://v2.api-m.com/api/heisi");
        String url = jsonObject.getString("data");
        url = url.replace("https", "http");
        return url;
    }
}
