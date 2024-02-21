package com.ai.aigenerate.chat.tool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;

@Service
public class GoogleSearchService {

    @SneakyThrows
    public String googleSearch(String keyword) {
        int numResults = 10; // 返回结果数量
        String languageCode = "cn"; // 语言设置（中文）
        String url = String.format("https://www.google.com/search?q=%s&num=%d&hl=%s", URLEncoder.encode(keyword), numResults, languageCode);
        Document document = Jsoup.connect(url).timeout(40000).get();
        Elements results = document.select("div.g"); // Google搜索结果的CSS选择器
        JSONArray jsonArray = new JSONArray();
        for (Element result : results) {
            // Extract the title and link of the result
            String title = result.select("h3").text();
            String link = result.select("h3").parents().attr("href");
            String snippet = result.select(".VwiC3b").text();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("title", title);
            jsonObject.put("url", link);
            jsonObject.put("content", snippet);
            // 将JSON对象添加到数组中
            jsonArray.add(jsonObject);
        }
        return jsonArray.toJSONString();
    }
}
