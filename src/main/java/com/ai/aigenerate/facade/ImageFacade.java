package com.ai.aigenerate.facade;

import com.ai.aigenerate.chat.tool.AliyunDrawService;
import com.ai.aigenerate.chat.tool.MjService;
import com.ai.aigenerate.chat.tool.StableDiffusionService;
import com.ai.aigenerate.chat.tool.TranslateService;
import com.ai.aigenerate.model.request.chat.DrawRequest;
import com.ai.aigenerate.model.request.stablediffusion.SdTextToImageRequest;
import com.ai.aigenerate.model.response.chat.DrawImageResponse;
import com.ai.aigenerate.model.response.mj.QueryTaskResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("ai")
@RestController
public class ImageFacade {

    @Autowired
    private AliyunDrawService aliyunDrawService;

    @Autowired
    private StableDiffusionService stableDiffusionService;

    @Autowired
    private MjService mjService;

    @Autowired
    private TranslateService translateService;

    @RequestMapping("drawImage")
    public DrawImageResponse drawImage(@RequestBody DrawRequest drawRequest){
        String url = aliyunDrawService.basicCall(drawRequest.getPrompt());
        DrawImageResponse drawImageResponse = new DrawImageResponse();
        drawImageResponse.setImageUrl(url);
        return drawImageResponse;
    }

    @RequestMapping("sd/textToImage")
    public DrawImageResponse textToImage(@RequestBody SdTextToImageRequest sdTextToImageRequest){
        String url = stableDiffusionService.textToImage(sdTextToImageRequest);
        DrawImageResponse drawImageResponse = new DrawImageResponse();
        drawImageResponse.setImageUrl(url);
        return drawImageResponse;
    }

    @RequestMapping("mj/textToImage")
    public DrawImageResponse createImage(@RequestBody SdTextToImageRequest sdTextToImageRequest){
        String prompt = translateService.translate(sdTextToImageRequest.getPrompt());
        QueryTaskResponse queryTaskResponse = mjService.addTask(prompt);
        DrawImageResponse drawImageResponse = new DrawImageResponse();
        if (queryTaskResponse == null) {
            return drawImageResponse;
        }
        drawImageResponse.setImageUrl(queryTaskResponse.getImageUrl());
        return drawImageResponse;
    }
}