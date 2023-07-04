package com.ai.aigenerate.model.response.mj;

import lombok.Data;

import java.util.Date;

@Data
public class QueryTaskResponse {

    public String action;

    public String id;

    public String prompt;

    public String promptEn;

    public String description;

    public String state;

    public Date submitTime;

    public Date startTime;

    public Date finishTime;

    public String imageUrl;

    public String status;

    public String progress;

    public String failReason;


}
