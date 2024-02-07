package com.ai.aigenerate.chat.tool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class CrawlerAiNewsService {

    @SneakyThrows
    public String getAiNews() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM月dd日");
        ZoneId shanghaiZoneId = ZoneId.of("Asia/Shanghai");

        // 获取今天的日期（上海时区）
        LocalDate today = LocalDate.now(shanghaiZoneId);
        String formattedToday = today.format(formatter);

        // 获取昨天的日期
        LocalDate yesterday = today.minusDays(1);
        String formattedYesterday = yesterday.format(formatter);

        // 获取前天的日期
        LocalDate dayBeforeYesterday = today.minusDays(2);
        String formattedDayBeforeYesterday = dayBeforeYesterday.format(formatter);

        String news = getJson(formattedToday,formattedYesterday);
        if (StringUtils.isEmpty(news)){
            news = getJson(formattedYesterday,formattedDayBeforeYesterday);
        }
        return news;
    }

    public String getJson(String formattedYesterday,String formattedDayBeforeYesterday) {
        try {
            // 创建HttpGet对象，设置要请求的URL
            Document document = Jsoup.connect("https://ai-bot.cn/daily-ai-news/").get();

            Element startDateElement = document.select("div.news-date:contains(" + formattedYesterday + ")").first();

            // Find the element for the end date
            Element endDateElement = document.select("div.news-date:contains(" + formattedDayBeforeYesterday + ")").first();
            JSONArray jsonArray = new JSONArray();

            // Ensure both dates exist
            if (startDateElement != null && endDateElement != null) {
                // Elements that follow the start date and precede the end date
                Elements newsItems = new Elements();

                Element nextElement = startDateElement.nextElementSibling();
                while (nextElement != null && !nextElement.hasSameValue(endDateElement)) {
                    if (nextElement.hasClass("news-item")) {
                        newsItems.add(nextElement);
                    }
                    nextElement = nextElement.nextElementSibling();
                }

                // Now newsItems contains all the desired elements
                for (Element newsItem : newsItems) {
                    Element link = newsItem.select("a").first();
                    Element summary = newsItem.select("p.text-muted.text-sm").first();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Title", link.text());
                    jsonObject.put("URL", link.attr("href"));
                    jsonObject.put("Summary", summary.text());
                    jsonArray.add(jsonObject);
                }
            } else {
                log.error("One of the date elements could not be found.");
                return null;
            }
            return jsonArray.toJSONString();
        }catch (Exception e){
            log.error("",e);
            return null;
        }
    }

}
