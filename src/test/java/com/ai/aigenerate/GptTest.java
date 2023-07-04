package com.ai.aigenerate;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ai.aigenerate.model.request.mail.EmailRequest;
import com.ai.aigenerate.utils.MailUtils;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.*;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.interceptor.DynamicKeyOpenAiAuthInterceptor;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import com.unfbx.chatgpt.interceptor.OpenAiResponseInterceptor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GptTest {

    private OpenAiClient openAiClient;
    private OpenAiStreamClient openAiStreamClient;

    @Before
    public void before() {
        //可以为null
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
        //！！！！千万别再生产或者测试环境打开BODY级别日志！！！！
        //！！！生产或者测试环境建议设置为这三种级别：NONE,BASIC,HEADERS,！！！
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
//                .proxy(proxy)
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(new OpenAiResponseInterceptor())
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        openAiClient = OpenAiClient.builder()
                //支持多key传入，请求时候随机选择
                .apiKey(Arrays.asList(""))
                //自定义key的获取策略：默认KeyRandomStrategy
                .keyStrategy(new KeyRandomStrategy())
                .authInterceptor(new DynamicKeyOpenAiAuthInterceptor())
                .okHttpClient(okHttpClient)
                //自己做了代理就传代理地址，没有可不不传,(关注公众号回复：openai ，获取免费的测试代理地址)
                .build();

        openAiStreamClient = OpenAiStreamClient.builder()
                //支持多key传入，请求时候随机选择
                .apiKey(Arrays.asList(""))
                //自定义key的获取策略：默认KeyRandomStrategy
                .keyStrategy(new KeyRandomStrategy())
                .authInterceptor(new DynamicKeyOpenAiAuthInterceptor())
                .okHttpClient(okHttpClient)
                //自己做了代理就传代理地址，没有可不不传,(关注公众号回复：openai ，获取免费的测试代理地址)
                .apiHost("https://dgr.life/")
                .build();
    }

    /**
     * 阻塞输出日志如下：
     *
     * [main] INFO com.unfbx.chatgpt.OpenAiClientFunctionTest - 自定义的方法返回值：词语：苹果
     *
     * 用途：苹果是一种水果，具有多种用途。以下是苹果的几种常见用途：
     *
     * 1. 直接吃：苹果可以直接食用，具有清爽的口感和丰富的营养成分，是一种健康的零食选择。
     *
     * 2. 做沙拉：苹果可以切成块状或丝状，加入其他蔬菜和调味料，制作成沙拉。苹果的甜脆口感可以为沙拉增添口感和风味。
     *
     * 3. 售卖：苹果是一种常见的水果，可以被商家售卖。人们可以购买苹果作为食物或礼物，满足自己或他人的需求。
     *
     * 总之，苹果是一种多功能的水果，可以直接食用，也可以用于制作沙拉，同时也是一种常见的商业商品。
     */
    @Test
    public void chatFunction() {

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
        ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);

        ChatChoice chatChoice = chatCompletionResponse.getChoices().get(0);
        log.info("构造的方法值：{}", chatChoice.getMessage().getFunctionCall());
        log.info("构造的方法名称：{}", chatChoice.getMessage().getFunctionCall().getName());
        log.info("构造的方法参数：{}", chatChoice.getMessage().getFunctionCall().getArguments());
        WordParam wordParam = JSONUtil.toBean(chatChoice.getMessage().getFunctionCall().getArguments(), WordParam.class);
        String oneWord = getOneWord(wordParam);

        FunctionCall functionCall = FunctionCall.builder()
                .arguments(chatChoice.getMessage().getFunctionCall().getArguments())
                .name("getOneWord")
                .build();
        Message message2 = Message.builder().role(Message.Role.ASSISTANT).content("方法参数").functionCall(functionCall).build();
        String content
                = "{ " +
                "\"wordLength\": \"3\", " +
                "\"language\": \"zh\", " +
                "\"物品\": \"" + oneWord + "\"," +
                "\"oneWord\": [\"直接吃\", \"做沙拉\", \"售卖\"]" +
                "}";
        Message message3 = Message.builder().role(Message.Role.FUNCTION).name("getOneWord").content(content).build();
        List<Message> messageList = Arrays.asList(message, message2, message3);
        ChatCompletion chatCompletionV2 = ChatCompletion
                .builder()
                .messages(messageList)
                .model(ChatCompletion.Model.GPT_3_5_TURBO_16K_0613.getName())
                .build();
        ChatCompletionResponse chatCompletionResponseV2 = openAiClient.chatCompletion(chatCompletionV2);
        log.info("自定义的方法返回值：{}",chatCompletionResponseV2.getChoices().get(0).getMessage().getContent());
    }


    /**
     * 获取一个词语
     * @param wordParam
     * @return
     */
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

    @Test
    public void testInput() {
        System.out.println(getOneWord(WordParam.builder().wordLength(2).language("zh").build()));
    }

    @Test
    public void testSendMail(){
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setServerName("smtp.qq.com");
        emailRequest.setPort("465");
        emailRequest.setUserName("2425428393@qq.com");
        emailRequest.setPassword("ufzvfouekfxwebai");
        emailRequest.setSenderName("AI小助手");
        emailRequest.setEmailAddress("2425428393@qq.com");
        emailRequest.setSubject("测试邮件");
        emailRequest.setTo("419820659@qq.com");
        emailRequest.setContent("test");
        MailUtils.sendMail(emailRequest);
    }

    @Data
    @Builder
    static class WordParam {
        private int wordLength;
        @Builder.Default
        private String language = "zh";
    }

    @Test
    public void streamChatFunction() {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        ConsoleEventSourceListenerV2 eventSourceListener = new ConsoleEventSourceListenerV2(countDownLatch);

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
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        openAiStreamClient.streamChatCompletion(chatCompletionV2, new ConsoleEventSourceListenerV2(countDownLatch));
        try {
            countDownLatch1.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
