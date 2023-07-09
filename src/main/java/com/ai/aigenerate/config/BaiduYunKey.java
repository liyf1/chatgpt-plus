package com.ai.aigenerate.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class BaiduYunKey {

    @Value("${baidu.weather.accessKey:}")
    private String weatherAccessKey;

    @Value("${baidu.weather.secretKey:}")
    private String weatherSecretKey;
}
