package com.ai.aigenerate.model.request.mj;

import lombok.Data;

@Data
public class CreateTaskRequest {

    private String base64;

    private String notifyHook;

    private String prompt;

    private String state;
}
