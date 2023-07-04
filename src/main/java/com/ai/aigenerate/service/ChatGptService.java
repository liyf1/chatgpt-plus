package com.ai.aigenerate.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ai.aigenerate.chat.FunctionEventSourceListener;
import com.ai.aigenerate.chat.tool.MjService;
import com.ai.aigenerate.config.GptFunctionConfig;
import com.ai.aigenerate.model.request.mail.EmailRequest;
import com.ai.aigenerate.model.request.mj.CreateTaskRequest;
import com.ai.aigenerate.model.request.mail.ImageMail;
import com.ai.aigenerate.model.response.mj.QueryTaskResponse;
import com.ai.aigenerate.utils.MailUtils;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.*;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.interceptor.DynamicKeyOpenAiAuthInterceptor;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import com.unfbx.chatgpt.interceptor.OpenAiResponseInterceptor;
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description: Test
 */
@Slf4j
@Service
@Deprecated
public class ChatGptService {

    private OpenAiClient openAiClient;

    @Autowired
    private MjService mjService;

    @Autowired
    private GptFunctionConfig gptFunctionConfig;

    private OpenAiStreamClient openAiStreamClient;

    @PostConstruct
    public void init() {
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
                //自己做了代理就传代理地址，没有可不不传,(关注公众号回复：openai ，获取免费的测试代理地址)
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

    public void function(String prompt) {
        Message message = Message.builder().role(Message.Role.USER).content(prompt).build();

        Functions imageFunction = getImageFunction();
        Functions sendMailFunction = getSendMailFunction();
        List<Message> messages = new ArrayList<>();
        messages.add(message);
        ChatCompletion chatCompletion = ChatCompletion
                .builder()
                .messages(messages)
                .functions(Arrays.asList(imageFunction, sendMailFunction))
                .functionCall("auto")
                .model(ChatCompletion.Model.GPT_3_5_TURBO_16K_0613.getName())
                .build();
        ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
        String rs = doHandler(chatCompletion, chatCompletionResponse.getChoices().get(0), messages);
    }

    public String doHandler(ChatCompletion chatCompletion, ChatChoice chatChoice, List<Message> messageList) {
        if (null == chatChoice.getMessage().getFunctionCall()) {
            return chatChoice.getMessage().getContent();
        }
        log.info("构造的方法值：{}", chatChoice.getMessage().getFunctionCall());
        log.info("构造的方法名称：{}", chatChoice.getMessage().getFunctionCall().getName());
        log.info("构造的方法参数：{}", chatChoice.getMessage().getFunctionCall().getArguments());
        if (chatChoice.getMessage().getFunctionCall().getName().equals("createImage")) {
            //调用自定义方法
            doImageHandler(chatChoice, messageList);
        } else if (chatChoice.getMessage().getFunctionCall().getName().equals("sendMail")) {
            //调用自定义方法
            doSendMailHandler(chatChoice, messageList);
        }
        return doHandler(chatCompletion, openAiClient.chatCompletion(chatCompletion).getChoices().get(0), messageList);
    }

    public void doImageHandler(ChatChoice chatChoice, List<Message> messageList) {
        CreateTaskRequest createTaskRequest = JSONUtil.toBean(chatChoice.getMessage().getFunctionCall().getArguments(), CreateTaskRequest.class);
        String url = createImage(createTaskRequest.getPrompt());

        FunctionCall functionCall = FunctionCall.builder()
                .arguments(chatChoice.getMessage().getFunctionCall().getArguments())
                .name("createImage")
                .build();
        Message message2 = Message.builder().role(Message.Role.ASSISTANT).content("方法参数").functionCall(functionCall).build();
        String content
                = "{ " +
                "\"这个是获取到的图片链接\": \"" + url + "\"" +
                "}";
        Message message3 = Message.builder().role(Message.Role.FUNCTION).name("createImage").content(content).build();
        messageList.add(message2);
        messageList.add(message3);
    }

    public void doSendMailHandler(ChatChoice chatChoice, List<Message> messageList) {
        EmailRequest emailRequest = JSONUtil.toBean(chatChoice.getMessage().getFunctionCall().getArguments(), EmailRequest.class);
        sendMail(emailRequest.getTo(), emailRequest.getContent(), emailRequest.getImageMailList());

        FunctionCall functionCall = FunctionCall.builder()
                .arguments(chatChoice.getMessage().getFunctionCall().getArguments())
                .name("sendMail")
                .build();
        Message message2 = Message.builder().role(Message.Role.ASSISTANT).content("方法参数").functionCall(functionCall).build();
        String content
                = "{ " +
                "\"发送情况\": \"发送完成\"" +
                "}";
        Message message3 = Message.builder().role(Message.Role.FUNCTION).name("sendMail").content(content).build();
        messageList.add(message2);
        messageList.add(message3);

    }

    private Functions getSendMailFunction() {
        JSONObject addressPrompt = new JSONObject();
        addressPrompt.putOpt("type", "string");
        addressPrompt.putOpt("description", "receiver email address，例如 xxxx@qq.com");
        JSONObject content = new JSONObject();
        content.putOpt("type", "string");
        content.putOpt("description", "mail content");

        JSONObject imageCid = new JSONObject();
        imageCid.putOpt("type", "string");
        imageCid.putOpt("description", "图片的<img src=\\\"cid:1\\\" />，其中1为图片的cid,参数名为cid");


        JSONObject url = new JSONObject();
        url.putOpt("type", "string");
        url.putOpt("description", "图片的url,参数名为url");

        JSONObject image = new JSONObject();
        image.putOpt("cid", imageCid);
        image.putOpt("url", url);

        JSONObject imageMailDtoList = new JSONObject();
        imageMailDtoList.putOpt("type", "array");
        imageMailDtoList.putOpt("items", image);
        imageMailDtoList.putOpt("description", "image的list,参数分别为cid和url");
        //参数
        JSONObject properties = new JSONObject();
        properties.putOpt("to", addressPrompt);
        properties.putOpt("content", content);
        properties.putOpt("imageMailDtoList", imageMailDtoList);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("to", "content")).build();
        Functions sendMailfunction = Functions.builder()
                .name("sendMail")
                .description("Function: Send mail, if the mail contains pictures, must appear in the body, and need to pass the cid and url of the picture, the picture in the body must like this <img src='cid:image'>")
                .parameters(parameters)
                .build();
        return sendMailfunction;
    }

    private static Functions getImageFunction() {
        JSONObject imagePrompt = new JSONObject();
        imagePrompt.putOpt("type", "string");
        imagePrompt.putOpt("description", "图片的描述,例如：一张猫的图片,统一转换为英文");
        //参数
        JSONObject properties = new JSONObject();
        properties.putOpt("prompt", imagePrompt);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("prompt")).build();
        Functions functions = Functions.builder()
                .name("createImage")
                .description("根据描述获取一张图片地址")
                .parameters(parameters)
                .build();
        return functions;
    }

    public String createImage(String prompt) {
        QueryTaskResponse queryTaskResponse = mjService.addTask(prompt);
        if (queryTaskResponse == null) {
            return "获取图片失败";
        }
        return queryTaskResponse.getImageUrl();
    }

    public void sendMail(String receiverAddress, String content, List<ImageMail> imageMailList) {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setServerName("smtp.qq.com");
        emailRequest.setPort("465");
        emailRequest.setUserName("");
        emailRequest.setPassword("");
        emailRequest.setSenderName("AI小助手");
        emailRequest.setEmailAddress("");
        emailRequest.setSubject("AI邮件通知");
        emailRequest.setTo(receiverAddress);
        emailRequest.setContent(content);
        emailRequest.setImageMailList(imageMailList);
        MailUtils.sendMail(emailRequest);
    }

    @Test
    public void streamChatFunction(SseEmitter sseEmitter) {
        FunctionEventSourceListener eventSourceListener = new FunctionEventSourceListener(sseEmitter);

        //模型：GPT_3_5_TURBO_16K_0613
        Message message = Message.builder().role(Message.Role.USER).content("给我输出一个长度为2的中文词语，并解释下词语对应物品的用途").build();
        //属性一
        JSONObject wordLength = new JSONObject();
        wordLength.putOpt("type", "number");
        wordLength.putOpt("description", "词语的长度");
        //属性二
        JSONObject language = new JSONObject();
        language.putOpt("type", "string");
        language.putOpt("enum", Arrays.asList("zh", "en"));
        language.putOpt("description", "语言类型，例如：zh代表中文、en代表英语");
        //参数
        JSONObject properties = new JSONObject();
        properties.putOpt("wordLength", wordLength);
        properties.putOpt("language", language);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("wordLength")).build();
        Functions functions = Functions.builder()
                .name("getOneWord")
                .description("获取一个指定长度和语言类型的词语")
                .parameters(parameters)
                .build();

        ChatCompletion chatCompletion = ChatCompletion
                .builder()
                .messages(Arrays.asList(message))
                .functions(Arrays.asList(functions))
                .functionCall("auto")
                .model(ChatCompletion.Model.GPT_3_5_TURBO_16K_0613.getName())
                .build();
        openAiStreamClient.streamChatCompletion(chatCompletion, eventSourceListener);
        eventSourceListener.getChatChoice();
        String args = eventSourceListener.getArgs();
        log.info("构造的方法参数：{}", args);
        WordParam wordParam = JSONUtil.toBean(args, WordParam.class);
        String oneWord = getOneWord(wordParam);

        FunctionCall functionCall = FunctionCall.builder()
                .arguments(args)
                .name("getOneWord")
                .build();
        Message message2 = Message.builder().role(Message.Role.ASSISTANT).content("方法参数").functionCall(functionCall).build();
        String content
                = "{ " +
                "\"wordLength\": \"3\", " +
                "\"language\": \"zh\", " +
                "\"word\": \"" + oneWord + "\"," +
                "\"用途\": [\"直接吃\", \"做沙拉\", \"售卖\"]" +
                "}";
        Message message3 = Message.builder().role(Message.Role.FUNCTION).name("getOneWord").content(content).build();
        List<Message> messageList = Arrays.asList(message, message2, message3);
        ChatCompletion chatCompletionV2 = ChatCompletion
                .builder()
                .messages(messageList)
                .model(ChatCompletion.Model.GPT_3_5_TURBO_16K_0613.getName())
                .build();
        openAiStreamClient.streamChatCompletion(chatCompletionV2, eventSourceListener);
        eventSourceListener.getChatChoice();
    }

    public SseEmitter createSse(String uid) {
        //默认30秒超时,设置为0L则永不超时
        SseEmitter sseEmitter = new SseEmitter(0l);
        //完成后回调
        sseEmitter.onCompletion(() -> {
            log.info("[{}]结束连接...................", uid);
            //LocalCache.CACHE.remove(uid);
        });
        //超时回调
        sseEmitter.onTimeout(() -> {
            log.info("[{}]连接超时...................", uid);
        });
        //异常回调
        sseEmitter.onError(
                throwable -> {
                    try {
                        log.info("[{}]连接异常,{}", uid, throwable.toString());
                        sseEmitter.send(SseEmitter.event()
                                .id(uid)
                                .name("发生异常！")
                                .data(Message.builder().content("发生异常请重试！").build())
                                .reconnectTime(3000));
                        //LocalCache.CACHE.put(uid, sseEmitter);
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
        //LocalCache.CACHE.put(uid, sseEmitter);
        log.info("[{}]创建sse连接成功！", uid);
        return sseEmitter;
    }

    @Data
    @Builder
    static class WordParam {
        private int wordLength;
        @Builder.Default
        private String language = "zh";
    }

    public String getOneWord(WordParam wordParam) {

        List<String> zh = Arrays.asList("大香蕉", "哈密瓜", "苹果");
        List<String> en = Arrays.asList("apple", "banana", "cantaloupe");
        if (wordParam.getLanguage().equals("zh")) {
            for (String e : zh) {
                if (e.length() == wordParam.getWordLength()) {
                    return e;
                }
            }
        }
        if (wordParam.getLanguage().equals("en")) {
            for (String e : en) {
                if (e.length() == wordParam.getWordLength()) {
                    return e;
                }
            }
        }
        return "西瓜";
    }
}
