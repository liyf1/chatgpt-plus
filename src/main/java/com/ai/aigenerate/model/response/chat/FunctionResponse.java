package com.ai.aigenerate.model.response.chat;

import lombok.Data;

@Data
public class FunctionResponse {

    private String functionName;

    private String functionDefinition;
}
