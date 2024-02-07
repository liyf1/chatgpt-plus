package com.ai.aigenerate.chat.custom;

import com.ai.aigenerate.chat.AbstractGptFunctionHandler;
import com.ai.aigenerate.chat.tool.CopyWritingService;
import com.unfbx.chatgpt.entity.chat.Functions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KfcFunctionHandler extends AbstractGptFunctionHandler<Object> {

    @Autowired
    private CopyWritingService copyWritingService;

    @Override
    public String doHandle(String paramJson) {
        return "{\"文案\":\""+copyWritingService.getKfcText()+"\"}";
    }

    @Override
    public Functions getFunction() {
        Functions functions = Functions.builder()
                .name("getCrazyKfc")
                .description("获取疯狂星期四的文案，得到的结果不要做修饰直接返回")
                .build();
        return functions;
    }
}
