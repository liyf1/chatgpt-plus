package com.ai.aigenerate.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class MailConfig {

    @Value("${mail.password:}")
    private String password;

    @Value("${mail.port:}")
    private String port;

    @Value("${mail.username:}")
    private String username;

    @Value("${mail.host:}")
    private String host;

    @Value("${mail.subject:}")
    private String subject;
}
