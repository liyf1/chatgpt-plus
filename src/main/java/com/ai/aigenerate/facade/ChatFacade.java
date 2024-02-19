package com.ai.aigenerate.facade;

import com.ai.aigenerate.chat.LinkAiChatService;
import com.ai.aigenerate.chat.tool.CopyWritingService;
import com.ai.aigenerate.chat.tool.CountDownHourService;
import com.ai.aigenerate.model.request.chat.ChatRequest;
import com.ai.aigenerate.chat.ChatService;
import com.ai.aigenerate.model.request.chat.LinkAiChatRequest;
import com.ai.aigenerate.model.response.BeCommonResponse;
import com.ai.aigenerate.model.response.chat.ChatResponse;
import com.ai.aigenerate.model.response.chat.FunctionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
    private LinkAiChatService linkAiChatService;

    @Autowired
    private CountDownHourService countDownHourService;

    @Autowired
    private CopyWritingService copyWritingService;

    @Autowired
    @Qualifier("streamThreadPool")
    private Executor executor;

    @Value("${chatgpt.api.token:}")
    private String token;

    @PostMapping("chat")
    public ChatResponse chat(@RequestBody ChatRequest chatRequest){
        if (chatRequest.getToken() == null || !chatRequest.getToken().equals(token)){
            throw new RuntimeException("token error");
        }
        return chatService.chat(chatRequest);
    }

    @PostMapping("chatStream")
    public SseEmitter chatStream(@RequestBody ChatRequest chatRequest){
        if (chatRequest.getToken() == null || !chatRequest.getToken().equals(token)){
            throw new RuntimeException("token error");
        }
        SseEmitter sseEmitter = chatService.createSse(chatRequest.getRequestId());
        executor.execute(() -> {
            if ("gpt-4-vision-preview".equals(chatRequest.getModel())) {
                chatService.pictureChatStream(chatRequest, sseEmitter);
            }else {
                chatService.chatStream(chatRequest,sseEmitter);
            }
        });
        return sseEmitter;
    }

    @PostMapping("auto/chatStream")
    public SseEmitter autoChatStream(@RequestBody ChatRequest chatRequest){
        if (chatRequest.getToken() == null || !chatRequest.getToken().equals(token)){
            throw new RuntimeException("token error");
        }
        SseEmitter sseEmitter = chatService.createSse(chatRequest.getRequestId());
        executor.execute(() -> {
            if ("gpt-4-vision-preview".equals(chatRequest.getModel())) {
                chatService.pictureChatStream(chatRequest, sseEmitter);
            }else {
                chatService.autoChatStream(chatRequest, sseEmitter);
            }
        });
        return sseEmitter;
    }

    @PostMapping("auto/chat")
    public ChatResponse chatDefaultFunction(@RequestBody ChatRequest chatRequest){
        if (chatRequest.getToken() == null || !chatRequest.getToken().equals(token)){
            throw new RuntimeException("token error");
        }
        return chatService.chatDefaultFunction(chatRequest);
    }

    @PostMapping("/knowledgeBase/chat")
    public ChatResponse knowledgeBaseChat(@RequestBody LinkAiChatRequest chatRequest){
        if (chatRequest.getToken() == null || !chatRequest.getToken().equals(token)){
            throw new RuntimeException("token error");
        }
        return linkAiChatService.chat(chatRequest);
    }

    @PostMapping("/knowledgeBase/chatStream")
    public SseEmitter knowledgeBaseChatStream(@RequestBody LinkAiChatRequest chatRequest){
        if (chatRequest.getToken() == null || !chatRequest.getToken().equals(token)){
            throw new RuntimeException("token error");
        }
        SseEmitter sseEmitter = linkAiChatService.createSse(chatRequest.getRequestId());
        executor.execute(() -> {
            linkAiChatService.chatStream(chatRequest,sseEmitter);
        });
        return sseEmitter;
    }

    @GetMapping("queryFunction")
    public List<FunctionResponse> queryFunction(){
        return chatService.queryFunctionNameList();
    }

    @RequestMapping("countDownHour")
    public BeCommonResponse countDownHour(){
        String countDownHour = countDownHourService.countDownHour();
        return BeCommonResponse.builder().result(countDownHour).build();
    }

    @RequestMapping("queryWord")
    public BeCommonResponse queryWord(){
       String word = countDownHourService.queryWord();
        return BeCommonResponse.builder().result(word).build();
    }

    @RequestMapping("queryCrazyKfc")
    public BeCommonResponse queryCrazyKfc(){
        String fkc = copyWritingService.getKfcText();
        return BeCommonResponse.builder().result(fkc).build();
    }
}
