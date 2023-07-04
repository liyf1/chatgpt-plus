package com.ai.aigenerate.model.response.chat;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ChatResponse {

    private String status;

    private String result;

    private String data;
}
