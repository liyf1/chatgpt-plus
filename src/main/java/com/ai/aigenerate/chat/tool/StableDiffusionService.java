package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.model.request.stablediffusion.SdTextToImageRequest;
import com.ai.aigenerate.model.request.stablediffusion.TextToImageDTO;
import com.ai.aigenerate.model.response.stablediffusion.TextToImageRespDTO;
import com.ai.aigenerate.utils.HttpClientUtils;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import org.springframework.stereotype.Component;

@Component
public class StableDiffusionService {
    public String textToImage(SdTextToImageRequest sdTextToImageRequest){
        TextToImageDTO textToImageDTO = new TextToImageDTO();
        textToImageDTO.setKey("");
        textToImageDTO.setModel_id("midjourney");
        textToImageDTO.setPrompt(sdTextToImageRequest.getPrompt());
        textToImageDTO.setNegative_prompt("");
        textToImageDTO.setScheduler("EulerDiscreteScheduler");
        textToImageDTO.setWidth("1024");
        textToImageDTO.setHeight("1024");
        textToImageDTO.setSamples("1");
        textToImageDTO.setNum_inference_steps("30");
        textToImageDTO.setGuidance_scale(7.5);
        textToImageDTO.setWebhook(null);
        textToImageDTO.setTrack_id(null);
        JSONObject jsonObject = HttpClientUtils.httpPost("https://stablediffusionapi.com/api/v4/dreambooth", JSON.toJSONString(textToImageDTO));
        TextToImageRespDTO textToImageRespDTO = jsonObject.toJavaObject(TextToImageRespDTO.class);
        return textToImageRespDTO.getOutput().get(0);
    }
}
