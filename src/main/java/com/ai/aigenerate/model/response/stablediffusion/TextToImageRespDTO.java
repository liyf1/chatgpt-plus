package com.ai.aigenerate.model.response.stablediffusion;

import lombok.Data;

import java.util.List;

@Data
public class TextToImageRespDTO {

    private String status;

    private Long generationTime;

    private Long id;

    private List<String> output;

    private Meta meta;
}
