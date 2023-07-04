package com.ai.aigenerate.chat;

import com.ai.aigenerate.config.GptFunctionConfig;
import com.ai.aigenerate.model.request.chat.ChatRequest;
import com.ai.aigenerate.model.response.chat.ChatResponse;
import com.ai.aigenerate.utils.MdcUtil;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.*;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatService {

    @Autowired
    private GptFunctionFactory gptFunctionFactory;

    @Autowired
    private GptFunctionConfig gptFunctionConfig;

    private OpenAiClient openAiClient;

    private OpenAiStreamClient openAiStreamClient;

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
        openAiClient = OpenAiClient.builder()
                //支持多key传入，请求时候随机选择
                .apiKey(gptFunctionConfig.getChatgptApiKey())
                //自定义key的获取策略：默认KeyRandomStrategy
                .keyStrategy(new KeyRandomStrategy())
                .authInterceptor(new DynamicKeyOpenAiAuthInterceptor())
                .okHttpClient(okHttpClient)
                .build();
        openAiStreamClient = OpenAiStreamClient.builder()
                //支持多key传入，请求时候随机选择
                .apiKey(gptFunctionConfig.getChatgptApiKey())
                //自定义key的获取策略：默认KeyRandomStrategy
                .keyStrategy(new KeyRandomStrategy())
                .authInterceptor(new DynamicKeyOpenAiAuthInterceptor())
                .okHttpClient(okHttpClient)
                .build();
    }

    public ChatResponse chat(ChatRequest chatRequest){
        ChatResponse chatResponse = ChatResponse.builder().status("200").build();
        String traceId = MdcUtil.generateTraceId();
        MdcUtil.setTraceId(traceId);
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
            if (chatRequest.getIsFunction()) {
                chatCompletion.setFunctions(gptFunctionFactory.getFunctionsByFunctionNameList(chatRequest.getFunctionNameList()));
                chatCompletion.setFunctionCall("auto");
            }
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
            String rs = doHandler(chatCompletionResponse.getChoices().get(0));
            chatResponse.setResult(rs);
            log.info("traceId:{},成功获取结果,调用链路：{}", traceId, gptContext.getGptHandlerHistories());
        } catch (Exception e) {
            log.error("traceId:{},调用chat接口异常", traceId, e);
            chatResponse.setStatus("500");
        } finally {
            ContextMap.remove(traceId);
            MdcUtil.removeTraceId();
        }
        return chatResponse;
    }

    public String doHandler(ChatChoice chatChoice){
        String content = chatChoice.getMessage().getContent();
        if (null == chatChoice.getMessage().getFunctionCall()){
            return content;
        }
        log.info("构造的方法值：{}", chatChoice.getMessage().getFunctionCall());
        log.info("构造的方法名称：{}", chatChoice.getMessage().getFunctionCall().getName());
        log.info("构造的方法参数：{}", chatChoice.getMessage().getFunctionCall().getArguments());
        GptFunctionService gptFunctionService = gptFunctionFactory.getGptFunctionService(chatChoice.getMessage().getFunctionCall().getName());
        if (gptFunctionService == null){
            return content;
        }
        ChatChoice result = gptFunctionService.preHandle(chatChoice);
        return doHandler(result);
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
            sseEmitter.send(SseEmitter.event().reconnectTime(5000));
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("[{}]创建sse连接成功！", requestId);
        return sseEmitter;
    }


    public void chatStream(ChatRequest chatRequest, SseEmitter sseEmitter){
        String traceId = MdcUtil.generateTraceId();
        MdcUtil.setTraceId(traceId);
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
            if (chatRequest.getIsFunction()) {
                chatCompletion.setFunctions(gptFunctionFactory.getFunctionsByFunctionNameList(chatRequest.getFunctionNameList()));
                chatCompletion.setFunctionCall("auto");
            }
            GptStreamContext gptStreamContext = GptStreamContext.builder()
                    .gptHandlerHistories(new ArrayList<>())
                    .messages(messages)
                    .openAiStreamClient(openAiStreamClient)
                    .chatCompletion(chatCompletion)
                    .requestId(chatRequest.getRequestId())
                    .functionEventSourceListener(eventSourceListener)
                    .timeout(120000l)
                    .build();
            ContextMap.putStreamContext(traceId, gptStreamContext);
            openAiStreamClient.streamChatCompletion(chatCompletion, eventSourceListener);
            ChatChoice chatChoice = eventSourceListener.getChatChoice();
            doStreamFunction(chatChoice);
            log.info("traceId:{},成功获取结果,调用链路：{}", traceId, gptStreamContext.getGptHandlerHistories());
            ContextMap.remove(traceId);
        } catch (Exception e) {
            log.error("traceId:{},异常：{}", traceId, e);
        }finally {
            ContextMap.remove(traceId);
            MdcUtil.removeTraceId();
            sseEmitter.complete();
        }
    }

    public void doStreamFunction(ChatChoice chatChoice){
        if (null == chatChoice.getDelta().getFunctionCall()){
            return;
        }
        log.info("构造的方法值：{}", chatChoice.getDelta().getFunctionCall());
        log.info("构造的方法名称：{}", chatChoice.getDelta().getFunctionCall().getName());
        log.info("构造的方法参数：{}", chatChoice.getDelta().getFunctionCall().getArguments());
        GptFunctionService gptFunctionService = gptFunctionFactory.getGptFunctionService(chatChoice.getDelta().getFunctionCall().getName());
        if (gptFunctionService == null){
            log.error("未找到对应的方法：{}",chatChoice.getDelta().getFunctionCall().getName());
            return;
        }
        ChatChoice chatChoiceResult = gptFunctionService.streamHandle(chatChoice);
        doStreamFunction(chatChoiceResult);
    }

    public List<String> queryFunctionNameList(){
        return gptFunctionFactory.getFunctions().stream().map(Functions::getName).collect(Collectors.toList());
    }

}