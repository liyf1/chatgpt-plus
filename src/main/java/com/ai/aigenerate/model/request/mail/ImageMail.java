package com.ai.aigenerate.model.request.mail;

import lombok.Data;

import java.io.Serializable;

@Data
public class ImageMail implements Serializable {

    private static final long serialVersionUID = 1L;

    private String url;

    private String cid;//"好好工作<img src=\"cid:abcd\">"
}
