package com.ai.aigenerate.facade;

import com.ai.aigenerate.model.request.chat.ChatRequest;
import com.ai.aigenerate.chat.ChatService;
import com.ai.aigenerate.model.response.chat.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;
import java.util.concurrent.Executor;

@RequestMapping("ai")
@RestController
public class ChatFacade {

    @Autowired
    private ChatService chatService;

    @Autowired
    @Qualifier("streamThreadPool")
    private Executor executor;

    @PostMapping("chat")
    public ChatResponse chat(@RequestBody ChatRequest chatRequest){
        return chatService.chat(chatRequest);
    }

    @PostMapping("chatStream")
    public SseEmitter queryTask(@RequestBody ChatRequest chatRequest){
        SseEmitter sseEmitter = chatService.createSse(chatRequest.getRequestId());
        executor.execute(() -> {
            chatService.chatStream(chatRequest,sseEmitter);
        });
        return sseEmitter;
    }

    @GetMapping("queryFunction")
    public List<String> queryFunction(){
        return chatService.queryFunctionNameList();
    }
}
