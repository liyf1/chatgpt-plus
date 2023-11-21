package com.ai.aigenerate.model.response.stablediffusion;

import lombok.Data;

@Data
public class Meta {
    private String prompt;
    private String model_id;
    private String negative_prompt;
    private String scheduler;
    private String safety_checker;
    private Integer W;
    private Integer H;
    private Double guidance_scale;
    private Integer seed;
    private Integer steps;
    private Integer n_samples;
    private String full_url;
    private String instant_response;
    private String tomesd;
    private String upscale;
    private String multi_lingual;
    private String panorama;
    private String self_attention;
    private String use_karras_sigmas;
    private String algorithm_type;
    private String safety_checker_type;
    private String embeddings;
    private String vae;
    private String lora;
    private Integer lora_strength;
    private Integer clip_skip;
    private String temp;
    private String base64;
    private String file_prefix;

}
