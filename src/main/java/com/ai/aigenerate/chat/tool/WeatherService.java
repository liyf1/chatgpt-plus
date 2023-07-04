package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.config.BaiduYunKey;
import com.baidubce.http.ApiExplorerClient;
import com.baidubce.http.AppSigner;
import com.baidubce.http.HttpMethodName;
import com.baidubce.model.ApiExplorerRequest;
import com.baidubce.model.ApiExplorerResponse;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WeatherService {

    @Autowired
    private BaiduYunKey baiduYunKey;

    private final LoadingCache<String, String> weatherCache;

    public WeatherService() {
        // 初始化天气缓存
        weatherCache = CacheBuilder.newBuilder()
                // 指定缓存最大容量为1000个domain
                .maximumSize(1000)
                // 缓存项在1小时后过期
                .expireAfterWrite(3, TimeUnit.HOURS)
                // 指定缓存加载器
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String city) throws Exception {
                        // 如果缓存未命中，则需要重新创建名单
                        String result = query(city);
                        return result;
                    }
                });
    }

    @SneakyThrows
    public String getWeather(String city) {
        return weatherCache.get(city);
    }

    /**
     * https://apis.baidu.com/store/detail/d031401a-4081-4572-8dd7-aca64223197e
     * @param city
     * @return
     */
    private String query(String city) {
        String path = "http://gwgp-n6uzuwmjrou.n.bdcloudapi.com/weather/query";
        ApiExplorerRequest request = new ApiExplorerRequest(HttpMethodName.POST, path);
        request.setCredentials(baiduYunKey.getWeatherAccessKey(), baiduYunKey.getWeatherSecretKey());

        request.addHeaderParameter("Content-Type", "application/json;charset=UTF-8");

        request.addQueryParameter("city", city);
        request.addQueryParameter("cityid", "");
        request.addQueryParameter("citycode", "");
        request.addQueryParameter("location", "");
        request.addQueryParameter("ip", "");

        ApiExplorerClient client = new ApiExplorerClient(new AppSigner());

        try {
            ApiExplorerResponse response = client.sendRequest(request);
            // 返回结果格式为Json字符串
            log.info(response.getResult());
            return response.getResult();
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
