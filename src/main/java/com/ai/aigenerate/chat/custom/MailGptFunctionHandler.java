package com.ai.aigenerate.chat.custom;

import cn.hutool.json.JSONObject;
import com.ai.aigenerate.config.MailConfig;
import com.ai.aigenerate.model.request.mail.EmailRequest;
import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.model.request.mail.ImageMail;
import com.ai.aigenerate.utils.MailUtils;
import com.alibaba.fastjson2.JSON;
import com.unfbx.chatgpt.entity.chat.Functions;
import com.unfbx.chatgpt.entity.chat.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MailGptFunctionHandler extends AbstractGptFunctionHandler<EmailRequest> {

    @Autowired
    private MailConfig mailConfig;

    @Override
    public String doHandle(String paramJson) {
        EmailRequest emailRequest = JSON.parseObject(paramJson, EmailRequest.class);
        sendMail(emailRequest.getTo(), emailRequest.getContent(), emailRequest.getImageMailList());
        String content
                = "{ " +
                "\"发送情况\": \"发送完成\"" +
                "}";
        return content;
    }

    public void sendMail(String receiverAddress, String content, List<ImageMail> imageMailList){
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setServerName(mailConfig.getHost());
        emailRequest.setPort(mailConfig.getPort());
        emailRequest.setUserName(mailConfig.getUsername());
        emailRequest.setPassword(mailConfig.getPassword());
        emailRequest.setSenderName("AI小助手");
        emailRequest.setEmailAddress(mailConfig.getUsername());
        emailRequest.setSubject(mailConfig.getSubject());
        emailRequest.setTo(receiverAddress);
        emailRequest.setContent(content);
        emailRequest.setImageMailList(imageMailList);
        MailUtils.sendMail(emailRequest);
    }

    @Override
    public Functions getFunction() {
        JSONObject addressPrompt = new JSONObject();
        addressPrompt.putOpt("type", "string");
        addressPrompt.putOpt("description", "receiver email address，例如 xxxx@qq.com");
        JSONObject content = new JSONObject();
        content.putOpt("type", "string");
        content.putOpt("description", "mail content");

        JSONObject imageCid = new JSONObject();
        imageCid.putOpt("type", "string");
        imageCid.putOpt("description", "图片的<img src=\\\"cid:1\\\" />，其中1为图片的cid,参数名为cid");


        JSONObject url = new JSONObject();
        url.putOpt("type", "string");
        url.putOpt("description", "图片的url,参数名为url");

        JSONObject image = new JSONObject();
        image.putOpt("cid", imageCid);
        image.putOpt("url", url);

        JSONObject imageMailDtoList = new JSONObject();
        imageMailDtoList.putOpt("type", "array");
        imageMailDtoList.putOpt("items", image);
        imageMailDtoList.putOpt("description", "image的list,参数分别为cid和url");
        //参数
        JSONObject properties = new JSONObject();
        properties.putOpt("to", addressPrompt);
        properties.putOpt("content", content);
        properties.putOpt("imageMailDtoList", imageMailDtoList);
        Parameters parameters = Parameters.builder()
                .type("object")
                .properties(properties)
                .required(Arrays.asList("to","content")).build();
        Functions sendMailfunction = Functions.builder()
                .name("sendMail")
                .description("发送邮件, if the mail contains pictures, must appear in the body, and need to pass the cid and url of the picture, the picture in the body must like this <img src='cid:image'>")
                .parameters(parameters)
                .build();
        return sendMailfunction;    }
}
