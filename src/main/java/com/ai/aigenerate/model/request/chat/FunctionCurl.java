package com.ai.aigenerate.model.request.chat;

import lombok.Data;

@Data
public class FunctionCurl {

    /**
     * url
     */
    private String url;

    /**
     * type: post/get
     */
    private String type;
}
