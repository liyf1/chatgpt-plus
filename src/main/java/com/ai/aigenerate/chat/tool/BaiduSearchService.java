package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.utils.HttpClientUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.net.Proxy;


@Slf4j
@Service
public class BaiduSearchService {

    @Autowired
    private ProxyIpService proxyIpService;

    public String getBaiduSearchResult(String keyword) {
        String jsonResults = "";
            int getIpCount = 0;
            for (int i = 0; i < 3; i++) {
                if (StringUtils.isNotBlank(jsonResults) && !jsonResults.equals("[]")) {
                    break;
                }
                try {
                if (jsonResults.equals("[]")){
                    proxyIpService.clearProxyIpCache();
                    getIpCount++;
                }
                Proxy proxyCache = proxyIpService.getProxyIpCache();
                // 发送GET请求

                Document document = Jsoup.connect("https://www.baidu.com/s?wd=" + keyword).timeout(40000).proxy(proxyCache).get();

                // 解析返回结果
                Elements results = document.select("div.result");

                // 创建JSON数组
                JSONArray jsonArray = new JSONArray();

                // 遍历每个搜索结果
                for (Element result : results) {
                    // 提取标题和URL
                    String title = result.select("h3").first().text();
                    String url = result.select("h3 a").first().attr("href");
                    // 创建JSON对象
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("title", title);
                    jsonObject.put("url", url);
                    jsonObject.put("content", result.text());
                    // 将JSON对象添加到数组中
                    jsonArray.add(jsonObject);
                }

                // 将JSON数组转换为字符串
                jsonResults = jsonArray.toJSONString();
                log.info("获取IP次数{},IP信息{},爬取结果{}",getIpCount,proxyCache,jsonResults);
                } catch (Exception e) {
                    jsonResults = "[]";
                    log.error("获取百度搜索结果异常",e);
                }

            }
            log.info("success:",jsonResults);
        return jsonResults;
    }
}
