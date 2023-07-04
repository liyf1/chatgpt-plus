package com.ai.aigenerate.model.request.news;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NewsRequest {

    private String type;

    private Integer page;

    @JsonProperty("page_size")
    private Integer pageSize;

    @JsonProperty("is_filter")
    private Integer isFilter;
}
