package com.ai.aigenerate.facade;

import com.ai.aigenerate.chat.tool.VideoService;
import com.ai.aigenerate.model.response.BeCommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("video")
public class VideoFacade {

    @Autowired
    private VideoService videoService;

    @RequestMapping("moyu")
    public BeCommonResponse queryMoyuVideo(){
        String url = videoService.getVideoMyPaper();
        return BeCommonResponse.builder().result(url).build();
    }

    @RequestMapping("dance")
    public BeCommonResponse queryDanceVideo(){
        String url = videoService.getDanceVideo();
        return BeCommonResponse.builder().result(url).build();
    }
}
