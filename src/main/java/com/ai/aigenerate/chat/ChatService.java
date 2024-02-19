package com.ai.aigenerate.chat;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.ai.aigenerate.config.GptConfig;
import com.ai.aigenerate.constant.VoiceContent;
import com.ai.aigenerate.model.request.chat.ChatRequest;
import com.ai.aigenerate.model.response.chat.ChatResponse;
import com.ai.aigenerate.model.response.chat.FunctionResponse;
import com.ai.aigenerate.utils.MdcUtils;
import com.alibaba.fastjson.JSON;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.Tts.TextToSpeech;
import com.unfbx.chatgpt.entity.Tts.TtsFormat;
import com.unfbx.chatgpt.entity.Tts.TtsVoice;
import com.unfbx.chatgpt.entity.chat.*;
import com.unfbx.chatgpt.entity.whisper.Translations;
import com.unfbx.chatgpt.entity.whisper.Whisper;
import com.unfbx.chatgpt.entity.whisper.WhisperResponse;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.interceptor.DynamicKeyOpenAiAuthInterceptor;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import com.unfbx.chatgpt.interceptor.OpenAiResponseInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatService {

    @Autowired
    private GptFunctionFactory gptFunctionFactory;

    @Autowired
    private GptConfig gptConfig;

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
                .connectTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
                .build();
        openAiClient = OpenAiClient.builder()
                //支持多key传入，请求时候随机选择
                .apiKey(gptConfig.getChatgptApiKey())
                //自定义key的获取策略：默认KeyRandomStrategy
                .keyStrategy(new KeyRandomStrategy())
                .authInterceptor(new DynamicKeyOpenAiAuthInterceptor())
                .okHttpClient(okHttpClient)
                .build();
        openAiStreamClient = OpenAiStreamClient.builder()
                //支持多key传入，请求时候随机选择
                .apiKey(gptConfig.getChatgptApiKey())
                //自定义key的获取策略：默认KeyRandomStrategy
                .keyStrategy(new KeyRandomStrategy())
                .authInterceptor(new DynamicKeyOpenAiAuthInterceptor())
                .okHttpClient(okHttpClient)
                .build();
    }

    public ChatResponse chat(ChatRequest chatRequest){
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
                    .maxTokens(chatRequest.getMaxTokens() != null?chatRequest.getMaxTokens():4096)
                    .temperature(chatRequest.getTemperature() != null?chatRequest.getTemperature():0.2)
                    .topP(chatRequest.getTopP() != null?chatRequest.getTopP():1.0)
                    .n(chatRequest.getN() != null?chatRequest.getN():1)
                    .model(chatRequest.getModel() != null?chatRequest.getModel() : ChatCompletion.Model.GPT_3_5_TURBO_16K_0613.getName())
                    .build();
            if (chatRequest.getIsFunction() != null && chatRequest.getIsFunction()) {
                chatCompletion.setFunctions(gptFunctionFactory.getFunctionsByFunctionNameList(chatRequest.getFunctionNameList()));
                chatCompletion.setFunctionCall("auto");
            }
            GptContext gptContext = GptContext.builder()
                    .gptHandlerHistories(new ArrayList<>())
                    .messages(messages)
                    .openAiClient(openAiClient)
                    .chatCompletion(chatCompletion)
                    .requestId(chatRequest.getRequestId())
                    .timeout(1200000000l)
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
            MdcUtils.removeTraceId();
        }
        return chatResponse;
    }

    public ChatResponse chatDefaultFunction(ChatRequest chatRequest){
        ChatResponse chatResponse = ChatResponse.builder().status("200").build();
        List<Message> messages = new ArrayList<>();
        Message systemMessage = Message.builder().role(Message.Role.SYSTEM).content(gptConfig.getSystemPrompt()).build();
        messages.add(systemMessage);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(chatRequest.getMessages())){
            for (Message message1:chatRequest.getMessages()){
                if (StringUtils.isNotBlank(message1.getContent())){
                    messages.add(message1);
                }
            }
        }
        Message message = Message.builder().role(Message.Role.USER).content(chatRequest.getPrompt()).build();
        messages.add(message);
        String traceId = MdcUtils.generateTraceId();
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
            if (chatRequest.getIsFunction() != null && chatRequest.getIsFunction()) {
                List<String> functionList = autoFindFunction(chatRequest);
                if (!CollectionUtils.isEmpty(functionList)){
                    chatCompletion.setFunctions(gptFunctionFactory.getFunctionsByFunctionNameList(functionList));
                    chatCompletion.setFunctionCall("auto");
                }
            }
            GptContext gptContext = GptContext.builder()
                    .gptHandlerHistories(new ArrayList<>())
                    .messages(messages)
                    .openAiClient(openAiClient)
                    .chatCompletion(chatCompletion)
                    .requestId(chatRequest.getRequestId())
                    .timeout(120000l)
                    .build();
            MdcUtils.setTraceId(traceId);
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
            MdcUtils.removeTraceId();
        }
        return chatResponse;
    }

    public void autoChatStream(ChatRequest chatRequest, SseEmitter sseEmitter){
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
            if (chatRequest.getIsFunction() != null && chatRequest.getIsFunction()) {
                List<String> functionList = autoFindFunction(chatRequest);
                if (!CollectionUtils.isEmpty(functionList)){
                    chatCompletion.setFunctions(gptFunctionFactory.getFunctionsByFunctionNameList(functionList));
                    chatCompletion.setFunctionCall("auto");
                }
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
            //todo
            MdcUtils.setTraceId(traceId);
            openAiStreamClient.streamChatCompletion(chatCompletion, eventSourceListener);
            ChatChoice chatChoice = eventSourceListener.getChatChoice();
            doStreamFunction(chatChoice);
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

    public void pictureChatStream(ChatRequest chatRequest, SseEmitter sseEmitter){
        String traceId = MdcUtils.generateTraceId();
        MdcUtils.setTraceId(traceId);
        FunctionEventSourceListener eventSourceListener = new FunctionEventSourceListener(sseEmitter);
        Content textContent = Content.builder().text(chatRequest.getPrompt()).type(Content.Type.TEXT.getName()).build();
        ImageUrl imageUrl = ImageUrl.builder().url(chatRequest.getImageUrl()).build();
        Content imageContent = Content.builder().imageUrl(imageUrl).type(Content.Type.IMAGE_URL.getName()).build();
        List<Content> contentList = new ArrayList<>();
        contentList.add(textContent);
        contentList.add(imageContent);
        MessagePicture message = MessagePicture.builder().role(Message.Role.USER).content(contentList).build();
        ChatCompletionWithPicture chatCompletion = ChatCompletionWithPicture
                .builder()
                .messages(Collections.singletonList(message))
                .model(ChatCompletion.Model.GPT_4_VISION_PREVIEW.getName())
                .build();
        try {
            openAiStreamClient.streamChatCompletion(chatCompletion, eventSourceListener);
            ChatChoice chatChoice = eventSourceListener.getChatChoice();
            doStreamFunction(chatChoice);
            ContextMap.remove(traceId);
        } catch (Exception e) {
            log.error("traceId:{},异常：{}", traceId, e);
        }finally {
            ContextMap.remove(traceId);
            MdcUtils.removeTraceId();
            sseEmitter.complete();
        }
    }

    private List<String> autoFindFunction(ChatRequest chatRequest) {

        ChatRequest completionRequest = new ChatRequest();
        List<Message> roleList = new ArrayList<>();
        List<Functions> functions = gptFunctionFactory.getFunctions();
        JSONArray jsonArray = new JSONArray();
        for (Functions function:functions){
            JSONObject jsonObject = new JSONObject();
            jsonObject.putOpt("函数名",function.getName());
            jsonObject.putOpt("函数描述",function.getDescription());
            jsonArray.add(jsonObject);
        }
        Message systemMessage = Message.builder().role(Message.Role.SYSTEM).content("你现在是一个函数判断器，这是我的要求\n" +
                "1、请根据函数描述返回需要使用的函数\n" +
                "2、必须用json返回结果，例如[\"queryWeather\",\"sendMail\"]，不要输出额外的内容，没有命中就返回空数组\n" +
                "3、这是所有的函数定义："+jsonArray).build();
        Message userMessage = Message.builder().role(Message.Role.USER).content("将上海天气发送给4198123131@qq.com").build();
        Message assistantMessage = Message.builder().role(Message.Role.ASSISTANT).content("[\"queryWeather\",\"sendMail\"]").build();
        Message userMessage1 = Message.builder().role(Message.Role.USER).content("你是谁").build();
        Message assistantMessage1 = Message.builder().role(Message.Role.ASSISTANT).content("[]").build();
        roleList.add(systemMessage);
        roleList.add(userMessage);
        roleList.add(assistantMessage);
        roleList.add(userMessage1);
        roleList.add(assistantMessage1);
        completionRequest.setMessages(roleList);
        completionRequest.setPrompt(chatRequest.getPrompt());
        completionRequest.setRequestId(chatRequest.getRequestId());
        completionRequest.setIsFunction(false);
        completionRequest.setMaxTokens(12000);
        completionRequest.setModel(ChatCompletion.Model.GPT_3_5_TURBO_16K.getName());
        String result = chat(completionRequest).getResult();
        return JSON.parseArray(result,String.class);
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
            sseEmitter.send(SseEmitter.event());
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("[{}]创建sse连接成功！", requestId);
        return sseEmitter;
    }


    public void chatStream(ChatRequest chatRequest, SseEmitter sseEmitter){
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
            if (chatRequest.getIsFunction() != null && chatRequest.getIsFunction() && !CollectionUtils.isEmpty(chatRequest.getFunctionNameList())) {
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
            MdcUtils.removeTraceId();
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

    public List<FunctionResponse> queryFunctionNameList(){
        return gptFunctionFactory.getFunctions().stream().map(gptFunction -> {
            FunctionResponse functionResponse = new FunctionResponse();
            functionResponse.setFunctionName(gptFunction.getName());
            functionResponse.setFunctionDefinition(gptFunction.getDescription());
            return functionResponse;
        }).collect(Collectors.toList());
    }

    public String speechToTextTranslations(File file) {
        Translations translations = Translations.builder()
                .model(Whisper.Model.WHISPER_1.getName())
                .prompt("必须将结果翻译成中文返回")
                .temperature(0.2)
                .responseFormat(Whisper.ResponseFormat.JSON.getName())
                .build();
        //语音转文字+翻译
        WhisperResponse whisperResponse =
                openAiClient.speechToTextTranslations(file, translations);
        return whisperResponse.getText();
    }

    public File textToSpeed(String text,String voice) {
        TextToSpeech textToSpeech = TextToSpeech.builder()
                .model(TextToSpeech.Model.TTS_1.getName())
                .input(text)
                .voice(StringUtils.isNotBlank(voice)?voice:TtsVoice.NOVA.getName())
                .responseFormat(TtsFormat.MP3.getName())
                .build();
        File file = new File(gptConfig.getTtsPath() +Math.random()+".mp3");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        openAiClient.textToSpeech(textToSpeech, new Callback<ResponseBody>() {
            @SneakyThrows
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                InputStream inputStream = response.body().byteStream();
                //创建文件
                if (!file.exists()) {
                    if (!file.getParentFile().exists())
                        file.getParentFile().mkdir();
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        log.error("createNewFile IOException");
                    }
                }

                OutputStream os = null;
                try {
                    os = new BufferedOutputStream(new FileOutputStream(file));
                    byte data[] = new byte[8192];
                    int len;
                    while ((len = inputStream.read(data, 0, 8192)) != -1) {
                        os.write(data, 0, len);
                    }
                    countDownLatch.countDown();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (os != null) {
                            os.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return file;
    }
}