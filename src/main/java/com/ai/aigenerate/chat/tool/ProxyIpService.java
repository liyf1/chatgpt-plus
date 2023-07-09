package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.config.ProxyIpConfig;
import com.ai.aigenerate.utils.HttpClientUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class ProxyIpService {

    @Autowired
    private ProxyIpConfig proxyIpConfig;

    private final LoadingCache<String,Proxy> proxyIpCache;

    public ProxyIpService() {
        // 初始化天气缓存
        proxyIpCache = CacheBuilder.newBuilder()
                // 指定缓存最大容量为1000个domain
                .maximumSize(2)
                // 缓存项在1小时后过期
                .expireAfterWrite(1, TimeUnit.HOURS)
                // 指定缓存加载器
                .build(new CacheLoader<String,Proxy>() {
                    @Override
                    public Proxy load(String key) throws Exception {
                        // 如果缓存未命中，则需要重新创建名单
                        return getProxyIp();
                    }
                });
    }

    public Proxy getProxyIp() {
        Map map = new HashMap();
        map.put("signature", proxyIpConfig.getSignature());
        map.put("secret_id", proxyIpConfig.getSecretId());
        map.put("num", 1);
        String ipResult = HttpClientUtils.httpGetString("https://dps.kdlapi.com/api/getdps", map);
        String[] split = ipResult.split(":");
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(split[0], Integer.parseInt(split[1])));
        return proxy;
    }

    @SneakyThrows
    public synchronized Proxy getProxyIpCache() {
        return proxyIpCache.get("国内");
    }

    public synchronized void clearProxyIpCache() {
        proxyIpCache.refresh("国内");
    }

}
