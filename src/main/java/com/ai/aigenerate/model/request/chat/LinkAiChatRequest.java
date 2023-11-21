package com.ai.aigenerate.model.request.chat;

import com.unfbx.chatgpt.entity.chat.Message;
import lombok.Data;

import java.util.List;

@Data
public class LinkAiChatRequest {

    private String requestId;

    private String prompt;

    private Double temperature;

    private Integer n;

    private String model;

    private Double topP;

    private Integer maxTokens;

    private List<Message> messages;

    private String knowledgeBase;

    private String token;
}
