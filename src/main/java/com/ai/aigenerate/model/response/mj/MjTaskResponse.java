package com.ai.aigenerate.model.response.mj;

import lombok.Data;

@Data
public class MjTaskResponse {

    private String result;

    private Integer code;

    private String description;

    private Object properties;
}
