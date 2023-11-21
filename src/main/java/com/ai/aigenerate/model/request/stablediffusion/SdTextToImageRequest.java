package com.ai.aigenerate.model.request.stablediffusion;

import lombok.Data;

@Data
public class SdTextToImageRequest {

    private String prompt;

    private String model;
}
