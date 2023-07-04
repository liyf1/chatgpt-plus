package com.ai.aigenerate.chat;

import com.unfbx.chatgpt.entity.chat.ChatChoice;
import com.unfbx.chatgpt.entity.chat.Functions;

public interface GptFunctionService<T> {

    ChatChoice preHandle(ChatChoice chatChoice);

    ChatChoice streamHandle(ChatChoice chatChoice);

    Functions getFunction();
}
