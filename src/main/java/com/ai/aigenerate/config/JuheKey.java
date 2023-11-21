package com.ai.aigenerate.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class JuheKey {

    @Value("${juhe.news.key:}")
    private String newsKey;
}
