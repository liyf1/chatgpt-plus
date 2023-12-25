package com.ai.aigenerate.chat.custom;

import cn.hutool.json.JSONObject;
import com.ai.aigenerate.chat.tool.DallE3ImageService;
import com.ai.aigenerate.model.request.mj.CreateTaskRequest;
import com.ai.aigenerate.model.response.mj.QueryTaskResponse;
import com.ai.aigenerate.chat.tool.MjService;
import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.alibaba.fastjson2.JSON;
import com.unfbx.chatgpt.entity.chat.Functions;
import com.unfbx.chatgpt.entity.chat.Parameters;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MjGptFunctionHandler extends AbstractGptFunctionHandler<CreateTaskRequest> {

    @Autowired
    private MjService mjService;

    @Autowired
    private DallE3ImageService dallE3ImageService;

    @Override
    public String doHandle(String paramJson) {
        CreateTaskRequest createTaskRequest = JSON.parseObject(paramJson, CreateTaskRequest.class);
        String url = "";
        if (createTaskRequest.getImageType() != null && createTaskRequest.getImageType().equals("midjoureny")){
            url = createImage(createTaskRequest.getPrompt());
        }else {
            List<String> urls =  dallE3ImageService.generateImage(createTaskRequest.getPrompt(),1);
            url = CollectionUtils.isNotEmpty(urls) ? urls.get(0) : "";
        }
        String content
                = "{ " +
                "\"这个是获取到的图片链接\": \"" + url + "\"" +
                "}";
        return content;
    }

    private String createImage(String prompt){
        QueryTaskResponse queryTaskResponse = mjService.addTask(prompt);
        if (queryTaskResponse == null) {
            return "获取图片失败";
        }
        return queryTaskResponse.getImageUrl();
    }

    @Override
    public Functions getFunction() {
        JSONObject imagePrompt = new JSONObject();
        imagePrompt.putOpt("type", "string");
        imagePrompt.putOpt("description", "图片的描述,例如：一张猫的图片,统一转换为英文");

        JSONObject imageType = new JSONObject();
        imageType.putOpt("type", "string");
        imageType.putOpt("enum",Arrays.asList("midjoureny","dallE3"));
        imageType.putOpt("description", "画图方式, 默认dallE3");
        //参数
        JSONObject properties = new JSONObject();
        properties.putOpt("prompt", imagePrompt);
        properties.putOpt("type", imageType);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("prompt","type")).build();
        Functions functions = Functions.builder()
                .name("createImage")
                .description("如果需要生成图片，可以根据描述生成一张图片，返回为图片地址,不要变更返回的地址，否则会导致图片无法显示")
                .parameters(parameters)
                .build();
        return functions;
    }

}
