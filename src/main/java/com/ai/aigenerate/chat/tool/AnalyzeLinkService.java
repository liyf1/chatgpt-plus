package com.ai.aigenerate.chat.tool;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AnalyzeLinkService {

    @SneakyThrows
    public String analyzeLink(String link){
        StringBuilder str = new StringBuilder();
        // 从URL加载HTML文档
        Document doc = Jsoup.connect(link).get();
        str.append(doc.text());

        // 选择所有<p>元素并提取其文本内容
        Elements paragraphs = doc.getElementsByTag("p");
        for (Element p : paragraphs) {
            str.append(p.text());
        }
        log.info("----------------------字符串长度：{}",str.length());
        return str.toString();
    }

}
