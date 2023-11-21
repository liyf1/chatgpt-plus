package com.ai.aigenerate.model.request.stablediffusion;

import lombok.Data;

@Data
public class TextToImageDTO {

    private String key;
    private String model_id;
    private String prompt;
    private String negative_prompt;
    private String width;
    private String height;
    private String samples;
    private String num_inference_steps;
    private String safety_checker;
    private String enhance_prompt;
    private String seed;
    private Double guidance_scale;
    private String multi_lingual;
    private String panorama;
    private String self_attention;
    private String upscale;
    private String embeddings_model;
    private String lora_model;
    private String tomesd;
    private String clip_skip;
    private String use_karras_sigmas;
    private String vae;
    private String lora_strength;
    private String scheduler;
    private String webhook;
    private String track_id;
}
