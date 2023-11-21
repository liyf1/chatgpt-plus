package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.chat.ChatService;
import com.ai.aigenerate.model.request.chat.ChatRequest;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class TranslateService {

    @Autowired
    private ChatService chatService;

    public String translate(String prompt){
        ChatRequest completionRequest = new ChatRequest();
        Message systemMessage = Message.builder().role(Message.Role.SYSTEM).content("你现在是一个AI翻译官，你会将我发给你的文字转换为英文描述，以下是我的要求\n" +
                "1、所有返回均使用英文\n" +
                "2、不要输出除了翻译的文字之外的内容\n" +
                "3、我提供的任何语言都原封不动的转换为英文").build();
        List<Message> messageList = new ArrayList();
        messageList.add(systemMessage);
        completionRequest.setMessages(messageList);
        completionRequest.setPrompt(prompt);
        completionRequest.setRequestId(UUID.randomUUID().toString());
        completionRequest.setIsFunction(false);
        completionRequest.setMaxTokens(2000);
        completionRequest.setModel(ChatCompletion.Model.GPT_3_5_TURBO.getName());
        String result = chatService.chat(completionRequest).getResult();
        return result;
    }
}
