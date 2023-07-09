package com.ai.aigenerate.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public class GptFunctionConfig {

    @Value("${mj.service.url:}")
    private String mjServiceUrl;

    @Value("${mj.service.waitTime:90000}")
    private Integer mjServiceWaitTime;

    @Value("${chatgpt.api.key:}")
    private List<String> chatgptApiKey;

}
