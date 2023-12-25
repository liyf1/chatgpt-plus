package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.config.GptConfig;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.images.Image;
import com.unfbx.chatgpt.entity.images.ImageResponse;
import com.unfbx.chatgpt.entity.images.SizeEnum;
import com.unfbx.chatgpt.function.KeyRandomStrategy;
import com.unfbx.chatgpt.interceptor.DynamicKeyOpenAiAuthInterceptor;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import com.unfbx.chatgpt.interceptor.OpenAiResponseInterceptor;
import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class DallE3ImageService {

    @Autowired
    private GptConfig gptConfig;

    private OpenAiClient openAiClient;

    @PostConstruct
    public void init(){
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
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
    }

    public List<String> generateImage(String text,Integer n){
        ImageResponse imageResponse = generateImageByDall_e_3(text,n);
        List<String> urls = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(imageResponse.getData())){
            imageResponse.getData().forEach(image -> {
                urls.add(image.getUrl());
            });
        }
        return urls;
    }

    public ImageResponse generateImageByDall_e_3(String prompt,Integer n) {
        Image image = Image.builder()
                .responseFormat(com.unfbx.chatgpt.entity.images.ResponseFormat.URL.getName())
                .model(Image.Model.DALL_E_3.getName())
                .prompt(prompt)
                .n(n)
                .quality(Image.Quality.STANDARD.getName())
                .size(SizeEnum.size_1024_1792.getName())
                .style(Image.Style.NATURAL.getName())
                .build();
        return openAiClient.genImages(image);
    }
}
