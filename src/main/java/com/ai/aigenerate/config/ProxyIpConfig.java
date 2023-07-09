package com.ai.aigenerate.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ProxyIpConfig {

    @Value("${proxy.ip.signature:}")
    private String signature;

    @Value("${proxy.ip.secretId:}")
    private String secretId;
}
