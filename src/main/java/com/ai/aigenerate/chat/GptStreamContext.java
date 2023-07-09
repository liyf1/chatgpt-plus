package com.ai.aigenerate.chat;

import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class GptStreamContext{

    private String requestId;

    //todo 超时时间,毫秒
    private Long timeout;

    private OpenAiStreamClient openAiStreamClient;

    private ChatCompletion chatCompletion;

    private List<Message> messages;

    private List<GptHandlerHistory> gptHandlerHistories;

    private FunctionEventSourceListener functionEventSourceListener;

    private SseEmitter sseEmitter;

}
