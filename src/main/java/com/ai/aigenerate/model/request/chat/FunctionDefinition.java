package com.ai.aigenerate.model.request.chat;

import com.unfbx.chatgpt.entity.chat.Functions;
import lombok.Data;

@Data
public class FunctionDefinition {

    private Functions functions;

    private FunctionCurl functionCurl;
}
