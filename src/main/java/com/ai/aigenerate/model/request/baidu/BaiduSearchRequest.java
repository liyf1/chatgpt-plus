package com.ai.aigenerate.model.request.baidu;

import lombok.Data;

@Data
public class BaiduSearchRequest {

    private String keyword;

    private String length;
}
