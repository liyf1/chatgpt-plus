package com.ai.aigenerate.model.request.mail;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class EmailRequest {

    private String serverName;
    private String port;
    private String userName;
    private String password;
    private String senderName;
    private String emailAddress;
    private String subject;
    private String to;
    private String cC;
    private String content;

    private List<ImageMail> imageMailList;

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
