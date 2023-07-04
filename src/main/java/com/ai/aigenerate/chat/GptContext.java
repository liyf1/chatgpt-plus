package com.ai.aigenerate.chat;

import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GptContext {

    private String requestId;

    //todo 超时时间,毫秒
    private Long timeout;

    private OpenAiClient openAiClient;

    private ChatCompletion chatCompletion;

    private List<Message> messages;

    private List<GptHandlerHistory> gptHandlerHistories;
}
