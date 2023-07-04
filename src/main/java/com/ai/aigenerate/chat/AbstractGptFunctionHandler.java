package com.ai.aigenerate.chat;

import com.ai.aigenerate.utils.MdcUtil;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.OpenAiStreamClient;
import com.unfbx.chatgpt.entity.chat.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public abstract class AbstractGptFunctionHandler<T> implements GptFunctionService<T> {

    public ChatChoice preHandle(ChatChoice chatChoice){
        String requestId = MdcUtil.getTraceId();
        Functions functions = getFunction();
        GptContext gptContext = ContextMap.get(requestId);
        OpenAiClient openAiClient = gptContext.getOpenAiClient();
        ChatCompletion chatCompletion = gptContext.getChatCompletion();
        List<GptHandlerHistory> gptHandlerHistories = gptContext.getGptHandlerHistories();
        List<Message> messages = gptContext.getMessages();
        if (null == chatChoice.getMessage().getFunctionCall())
            return chatChoice;
        if (!chatChoice.getMessage().getFunctionCall().getName().equals(functions.getName())) {
            log.error("当前方法不匹配:{}", chatChoice.getMessage().getFunctionCall());
            return chatChoice;
        }
        String result = doHandle(chatChoice.getMessage().getFunctionCall().getArguments());
        FunctionCall functionCall = FunctionCall.builder()
                .arguments(chatChoice.getMessage().getFunctionCall().getArguments())
                .name(functions.getName())
                .build();
        Message message1 = Message.builder().role(Message.Role.ASSISTANT).content("方法参数").functionCall(functionCall).build();
        Message message2 = Message.builder().role(Message.Role.FUNCTION).name(functions.getName()).content(result).build();
        messages.add(message1);
        messages.add(message2);
        GptHandlerHistory gptHandlerHistory = GptHandlerHistory.builder().functionName(functions.getName())
                .functionParam(chatChoice.getMessage().getFunctionCall().getArguments()).result(result).build();
        gptHandlerHistories.add(gptHandlerHistory);
        return openAiClient.chatCompletion(chatCompletion).getChoices().get(0);
    }

    public ChatChoice streamHandle(ChatChoice chatChoice){
        String requestId = MdcUtil.getTraceId();
        Functions functions = getFunction();
        GptStreamContext gptStreamContext = ContextMap.getStreamContext(requestId);
        ChatCompletion chatCompletion = gptStreamContext.getChatCompletion();
        OpenAiStreamClient openAiStreamClient = gptStreamContext.getOpenAiStreamClient();
        if (null == chatChoice.getDelta().getFunctionCall())
            return chatChoice;
        if (!chatChoice.getDelta().getFunctionCall().getName().equals(functions.getName())) {
            log.error("当前方法不匹配:{}", chatChoice.getDelta().getFunctionCall());
            return chatChoice;
        }
        FunctionEventSourceListener functionEventSourceListener = gptStreamContext.getFunctionEventSourceListener();
        String args = chatChoice.getDelta().getFunctionCall().getArguments();
        log.info("构造的方法参数：{}", args);
        String result = doHandle(args);
        FunctionCall functionCall = FunctionCall.builder()
                .arguments(args)
                .name(functions.getName())
                .build();
        Message message1 = Message.builder().role(Message.Role.ASSISTANT).content("方法参数").functionCall(functionCall).build();
        Message message2 = Message.builder().role(Message.Role.FUNCTION).name(functions.getName()).content(result).build();
        List<Message> messages = gptStreamContext.getMessages();
        messages.add(message1);
        messages.add(message2);
        functionEventSourceListener.reset();
        openAiStreamClient.streamChatCompletion(chatCompletion,functionEventSourceListener );
        GptHandlerHistory gptHandlerHistory = GptHandlerHistory.builder().functionName(functions.getName())
                .functionParam(chatChoice.getDelta().getFunctionCall().getArguments()).result(result).build();
        gptStreamContext.getGptHandlerHistories().add(gptHandlerHistory);
        return functionEventSourceListener.getChatChoice();
    }


    public abstract String doHandle(String paramJson);

    public abstract Functions getFunction();


}
