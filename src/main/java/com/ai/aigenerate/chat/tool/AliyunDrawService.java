package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.utils.OssUtils;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class AliyunDrawService {

    private static final OkHttpClient CLIENT = new OkHttpClient();
    private static final String MODEL = "stable-diffusion-v1.5";
    private static final String PROMPT = "dog flying freely in the blue sky and white clouds";
    private static final String SIZE = "512*512";

    @SneakyThrows
    public String basicCall(String prompt) {
        ImageSynthesis is = new ImageSynthesis();
        ImageSynthesisParam param =
                ImageSynthesisParam.builder().apiKey("")
                        .model(MODEL)
                        .n(1)
                        .size(SIZE)
                        .prompt(prompt)
                        .negativePrompt("garfield")
                        .build();

        ImageSynthesisResult result = is.call(param);
        System.out.println(result);
        // save image to local files.
        for(Map<String, String> item :result.getOutput().getResults()){
            String paths = new URL(item.get("url")).getPath();
            String[] parts = paths.split("/");
            String fileName = parts[parts.length-1];
            Request request = new Request.Builder()
                    .url(item.get("url"))
                    .build();

            try (Response response = CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                Path file = Paths.get(fileName);
                Files.write(file, response.body().bytes());
                InputStream inputStream = new BufferedInputStream(file.toUri().toURL().openStream());
                return OssUtils.upload(inputStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        return null;
    }
}
