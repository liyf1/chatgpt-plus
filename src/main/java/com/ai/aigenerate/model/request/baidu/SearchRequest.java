package com.ai.aigenerate.model.request.baidu;

import lombok.Data;

@Data
public class SearchRequest {

    private String keyword;

    private String length;
}
