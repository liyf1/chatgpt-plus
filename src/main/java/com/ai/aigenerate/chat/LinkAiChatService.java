package com.ai.aigenerate.chat;

import com.ai.aigenerate.config.GptConfig;
import com.ai.aigenerate.constant.LinkAiContent;
import com.ai.aigenerate.model.request.chat.LinkAiChatRequest;
import com.ai.aigenerate.model.response.chat.ChatResponse;
import com.ai.aigenerate.utils.MdcUtils;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.interceptor.DynamicKeyOpenAiAuthInterceptor;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import com.unfbx.chatgpt.interceptor.OpenAiResponseInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LinkAiChatService {

    @Autowired
    private GptFunctionFactory gptFunctionFactory;

    @Autowired
    private GptConfig gptConfig;

    private Map<String,OpenAiClient> linkAiClientMap;

    private Map<String,OpenAiStreamClient> linkAiStreamClientMap;

    @PostConstruct
    public void init(){
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
        //！！！！千万别再生产或者测试环境打开BODY级别日志！！！！
        //！！！生产或者测试环境建议设置为这三种级别：NONE,BASIC,HEADERS,！！！
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new OpenAiResponseInterceptor())
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Map<String,String> linkAiApiKeyMap = gptConfig.getLinkAiApiKeyMap();
        linkAiClientMap = linkAiApiKeyMap.entrySet().stream().collect(HashMap::new, (m, v) -> m.put(v.getKey(), OpenAiClient.builder()
                //支持多key传入，请求时候随机选择
                .apiKey(Collections.singletonList(v.getValue()))
                .apiHost(LinkAiContent.LINK_AI_DOMAIN)
                //自定义key的获取策略：默认KeyRandomStrategy
                .keyStrategy(new KeyRandomStrategy())
                .authInterceptor(new DynamicKeyOpenAiAuthInterceptor())
                .okHttpClient(okHttpClient)
                .build()), HashMap::putAll);
        linkAiStreamClientMap = linkAiApiKeyMap.entrySet().stream().collect(HashMap::new, (m, v) -> m.put(v.getKey(), OpenAiStreamClient.builder()
                //支持多key传入，请求时候随机选择
                .apiKey(Collections.singletonList(v.getValue()))
                .apiHost(LinkAiContent.LINK_AI_DOMAIN)
                //自定义key的获取策略：默认KeyRandomStrategy
                .keyStrategy(new KeyRandomStrategy())
                .authInterceptor(new DynamicKeyOpenAiAuthInterceptor())
                .okHttpClient(okHttpClient)
                .build()), HashMap::putAll);
    }

    public ChatResponse chat(LinkAiChatRequest chatRequest){
        ChatResponse chatResponse = ChatResponse.builder().status("200").build();
        String traceId = MdcUtils.generateTraceId();
        MdcUtils.setTraceId(traceId);
        Message message = Message.builder().role(Message.Role.USER).content(chatRequest.getPrompt()).build();
        List<Message> messages = chatRequest.getMessages();
        if (messages == null)
            messages = new ArrayList<>();
        messages.add(message);
        try {
            ChatCompletion chatCompletion = ChatCompletion
                    .builder()
                    .messages(messages)
                    .maxTokens(chatRequest.getMaxTokens() != null?chatRequest.getMaxTokens():8000)
                    .temperature(chatRequest.getTemperature() != null?chatRequest.getTemperature():0.2)
                    .topP(chatRequest.getTopP() != null?chatRequest.getTopP():1.0)
                    .n(chatRequest.getN() != null?chatRequest.getN():1)
                    .model(chatRequest.getModel() != null?chatRequest.getModel() : ChatCompletion.Model.GPT_3_5_TURBO_16K_0613.getName())
                    .build();
            OpenAiClient openAiClient = linkAiClientMap.get(chatRequest.getKnowledgeBase());
            GptContext gptContext = GptContext.builder()
                    .gptHandlerHistories(new ArrayList<>())
                    .messages(messages)
                    .openAiClient(openAiClient)
                    .chatCompletion(chatCompletion)
                    .requestId(chatRequest.getRequestId())
                    .timeout(120000l)
                    .build();
            ContextMap.put(traceId, gptContext);
            ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
            String rs = chatCompletionResponse.getChoices().get(0).getMessage().getContent();
            chatResponse.setResult(rs);
            log.info("traceId:{},成功获取结果,调用链路：{}", traceId, gptContext.getGptHandlerHistories());
        } catch (Exception e) {
            log.error("traceId:{},调用chat接口异常", traceId, e);
            chatResponse.setStatus("500");
        } finally {
            ContextMap.remove(traceId);
            MdcUtils.removeTraceId();
        }
        return chatResponse;
    }

    public SseEmitter createSse(String requestId) {
        //默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(0l);
        //完成后回调
        sseEmitter.onCompletion(() -> {
            log.info("[{}]结束连接...................", requestId);
        });
        //超时回调
        sseEmitter.onTimeout(() -> {
            log.info("[{}]连接超时...................", requestId);
        });
        //异常回调
        sseEmitter.onError(
                throwable -> {
                    try {
                        log.info("[{}]连接异常,{}", requestId, throwable.toString());
                        sseEmitter.send(SseEmitter.event()
                                .id(requestId)
                                .name("发生异常！")
                                .data(Message.builder().content("发生异常请重试！").build())
                                .reconnectTime(3000));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
        try {
            sseEmitter.send(SseEmitter.event());
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("[{}]创建sse连接成功！", requestId);
        return sseEmitter;
    }


    public void chatStream(LinkAiChatRequest chatRequest, SseEmitter sseEmitter){
        String traceId = MdcUtils.generateTraceId();
        MdcUtils.setTraceId(traceId);
        FunctionEventSourceListener eventSourceListener = new FunctionEventSourceListener(sseEmitter);
        Message message = Message.builder().role(Message.Role.USER).content(chatRequest.getPrompt()).build();
        List<Message> messages = chatRequest.getMessages();
        if (messages == null)
            messages = new ArrayList<>();
        messages.add(message);
        try {
            ChatCompletion chatCompletion = ChatCompletion
                    .builder()
                    .messages(messages)
                    .maxTokens(chatRequest.getMaxTokens() != null?chatRequest.getMaxTokens():2048)
                    .temperature(chatRequest.getTemperature() != null?chatRequest.getTemperature():0.2)
                    .topP(chatRequest.getTopP() != null?chatRequest.getTopP():1.0)
                    .n(chatRequest.getN() != null?chatRequest.getN():1)
                    .model(chatRequest.getModel() != null?chatRequest.getModel() : ChatCompletion.Model.GPT_3_5_TURBO_16K_0613.getName())
                    .build();
            OpenAiStreamClient openAiClient = linkAiStreamClientMap.get(chatRequest.getKnowledgeBase());
            GptStreamContext gptStreamContext = GptStreamContext.builder()
                    .gptHandlerHistories(new ArrayList<>())
                    .messages(messages)
                    .openAiStreamClient(openAiClient)
                    .chatCompletion(chatCompletion)
                    .requestId(chatRequest.getRequestId())
                    .functionEventSourceListener(eventSourceListener)
                    .timeout(120000l)
                    .build();
            ContextMap.putStreamContext(traceId, gptStreamContext);
            openAiClient.streamChatCompletion(chatCompletion, eventSourceListener);
            log.info("traceId:{},成功获取结果,调用链路：{}", traceId, gptStreamContext.getGptHandlerHistories());
            ContextMap.remove(traceId);
        } catch (Exception e) {
            log.error("traceId:{},异常：{}", traceId, e);
        }finally {
            ContextMap.remove(traceId);
            MdcUtils.removeTraceId();
            sseEmitter.complete();
        }
    }

}
