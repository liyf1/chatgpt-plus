package com.ai.aigenerate.model.request.chat;

import com.unfbx.chatgpt.entity.chat.Message;
import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {

    private String requestId;

    private String prompt;

    private Double temperature;

    private Integer n;

    private String model;

    private Double topP;

    private Integer maxTokens;

    private Boolean isFunction;

    private List<Message> messages;

    private List<String> functionNameList;

    private List<FunctionDefinition> functionDefinitionList;

    private String token;

    private String imageUrl;

}
