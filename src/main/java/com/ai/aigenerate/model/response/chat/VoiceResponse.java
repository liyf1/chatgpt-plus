package com.ai.aigenerate.model.response.chat;

import lombok.Data;

@Data
public class VoiceResponse {

    private String audio;

    private String questionAsr;

    private String answerAsr;
}
