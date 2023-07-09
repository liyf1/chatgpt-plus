package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.utils.HttpClientUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
public class WeiboService {

    @Autowired
    private ProxyIpService proxyIpService;

    private static Map<String,String> typeMap = new HashMap<>();

    private final LoadingCache<String, String> weiboCache;

    public WeiboService() {
        // 初始化天气缓存
        weiboCache = CacheBuilder.newBuilder()
                // 指定缓存最大容量为1000个domain
                .maximumSize(10)
                // 缓存项在1小时后过期
                .expireAfterWrite(1, TimeUnit.HOURS)
                // 指定缓存加载器
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String type) throws Exception {
                        // 如果缓存未命中，则需要重新创建名单
                        String result = queryWeiboResult(type);
                        return result;
                    }
                });
    }

    static {
        typeMap.put("hotSearch", "https://tophub.today/n/KqndgxeLl9");
        typeMap.put("topic", "https://tophub.today/n/VaobJ98oAj");
        typeMap.put("importantNews", "https://tophub.today/n/Om4ejl3vxE");
        typeMap.put("movie", "https://tophub.today/n/DOvnNXqvEB");
        typeMap.put("entertainment", "https://tophub.today/n/3QeLwJEd7k");
    }

    @SneakyThrows
    public String getWeiboResult(String type){
        String result = weiboCache.get(type);
        if (StringUtils.isNotBlank(result)) {
            return result;
        }else {
            weiboCache.refresh(type);
            return weiboCache.get(type);
        }
    }

    private String queryWeiboResult(String type) {
        JSONArray jsonArray = new JSONArray();
        for (int count = 0; count < 3; count++) {
            if (jsonArray.size() > 0) {
                break;
            }
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = null;
            // 2.创建get请求，相当于在浏览器地址栏输入 网址
            HttpGet request = new HttpGet(typeMap.get(type));
            // 设置请求头，将爬虫伪装成浏览器
            request.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
            //如果有ip代理，可以加上如下代码
            Proxy proxy = proxyIpService.getProxyIpCache();
            InetSocketAddress inetSocketAddress = (InetSocketAddress) proxy.address();
            HttpHost host = new HttpHost(inetSocketAddress.getHostName(), inetSocketAddress.getPort());
            RequestConfig config = RequestConfig.custom().setProxy(host).setConnectTimeout(5000).build();
            request.setConfig(config);
            try {
                // 3.执行get请求，相当于在输入地址栏后敲回车键
                response = httpClient.execute(request);

                // 4.判断响应状态为200，进行处理
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    // 5.获取响应内容
                    HttpEntity httpEntity = response.getEntity();
                    String html = EntityUtils.toString(httpEntity, "utf-8");
                    // 6.Jsoup解析html
                    Document document = Jsoup.parse(html);
                    // 像js一样，通过标签获取title
                    Element item = document.getElementsByTag("tbody").first();
                    if (item == null) {
                        proxyIpService.clearProxyIpCache();
                        return "获取失败";
                    }
                    Elements items = item.getElementsByTag("tr");
                    int i = 0;
                    int topCount = 50;
                    for (Element tmp : items) {
                        Element rankEle = tmp.getElementsByTag("td").first();
                        Elements textEle = tmp.select(".al").select("a");
                        JSONObject jsonObject = new JSONObject();
                        //String herf = textEle.select("a").attr("href");
                        Elements td2 = items.get(i).getElementsByTag("td").next().next();
                        String td2Text = td2.text();
                        i++;
                        if (jsonArray.size() >= topCount) {
                            break;
                        }
                        jsonObject.put("序号", rankEle.text());
                        String title = textEle.text().replaceAll(" ", "%20");
                        jsonObject.put("标题", textEle.text());
                        jsonObject.put("链接地址", "https://s.weibo.com/weibo?q=%23" + title + "%23");
                        //1. 可以在中括号内加上任何想要删除的字符，实际上是一个正则表达式
                        String regExp = "[\n`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。， 、？\uE652]";
                        //2. 这里是将特殊字符换为空字符串,""代表直接去掉
                        String replace = "";
                        //3. 要处理的字符串
                        td2Text = td2Text.replaceAll(regExp, replace);
                        jsonObject.put("热度", td2Text);
                        jsonArray.add(jsonObject);
                        System.out.println(jsonObject);
                    }
                } else {
                    // 如果返回状态不是200，比如404（页面不存在）等，根据情况做处理，这里略
                    System.out.println("返回状态不是200");
                    System.out.println(EntityUtils.toString(response.getEntity(), "utf-8"));
                    proxyIpService.clearProxyIpCache();
                }
                log.info("代理ip:{},获取次数:{}，获取结果:{}", proxy, count ,jsonArray.toJSONString());
            } catch (Exception e) {
                log.error("获取微博热搜失败", e);
                proxyIpService.clearProxyIpCache();
            }
        }
        return jsonArray.toJSONString();
    }
}
