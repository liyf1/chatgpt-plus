package com.ai.aigenerate.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Component
public class GptConfig {

    @Value("${mj.service.url:http://localhost:8080}")
    private String mjServiceUrl;

    @Value("${mj.service.waitTime:90000}")
    private Integer mjServiceWaitTime;

    @Value("${chatgpt.api.key}")
    private List<String> chatgptApiKey;

    @Value("${linkai.api.key.map}")
    private String linkAiApiKeyMap;

    @Value("${system.prompt:}")
    private String systemPrompt;

    @Value("${tts.path:/Users/liyifan/Downloads/voice/}")
    private String ttsPath;

    @Value("${asr.path:/Users/liyifan/Downloads/asr/}")
    private String asrPath;

    @Value("${voice.api.token:6543213}")
    private String voiceApiToken;

    @Value("${voice.prompt.system:请使用中文交流，回答要求尽可能简短，不能超过100个字}")
    private String voicePromptSystem;

    public Map<String,String> getLinkAiApiKeyMap(){
        Map<String,String> map = new HashMap<>();
        String[] split = linkAiApiKeyMap.split(",");
        for (String s : split) {
            String[] split1 = s.split(":");
            map.put(split1[0],split1[1]);
        }
        return map;
    }

}
