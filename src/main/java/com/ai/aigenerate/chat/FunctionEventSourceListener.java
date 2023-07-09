package com.ai.aigenerate.chat;

import cn.hutool.json.JSONUtil;
import com.unfbx.chatgpt.entity.chat.ChatChoice;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static com.unfbx.chatgpt.utils.TikTokensUtil.tokens;

@Slf4j
public class FunctionEventSourceListener extends EventSourceListener {
    @Getter
    String args = "";
    private CountDownLatch countDownLatch;

    private SseEmitter sseEmitter;

    private ChatChoice chatChoice;

    private Boolean isResponse;

    public FunctionEventSourceListener(SseEmitter sseEmitter) {
        this.countDownLatch = new CountDownLatch(1);
        this.sseEmitter = sseEmitter;
        chatChoice = null;
        isResponse = false;
    }

    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("OpenAI建立sse连接...");
        chatChoice = null;
        args = "";
    }

    @SneakyThrows
    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        log.info("OpenAI返回数据：{}", data);
        if (data.equals("[DONE]")) {
            log.info("OpenAI返回数据结束了");
            if (isResponse) {
                sseEmitter.send(SseEmitter.event()
                        .data("[DONE]"));
            }
            countDownLatch.countDown();
            log.info("OpenAI返回数据结束了");
            return;
        }
        ChatCompletionResponse chatCompletionResponse = JSONUtil.toBean(data, ChatCompletionResponse.class);
        if (chatChoice == null){
            chatChoice = chatCompletionResponse.getChoices().get(0);
        }
        if(Objects.nonNull(chatCompletionResponse.getChoices().get(0).getDelta().getFunctionCall())){
            args += chatCompletionResponse.getChoices().get(0).getDelta().getFunctionCall().getArguments();
            if (chatChoice.getDelta().getFunctionCall() != null) {
                chatChoice.getDelta().getFunctionCall().setArguments(args);
            }
        }else {
            if (chatCompletionResponse.getChoices().get(0).getDelta().getContent() != null) {
                isResponse = true;
                try {
                    sseEmitter.send(SseEmitter.event()
                            .data(data));
                } catch (Exception e) {
                    log.error("sse信息推送失败！");
                    eventSource.cancel();
                    e.printStackTrace();
                }
            }
        }
    }

    public void reset(){
        this.countDownLatch = new CountDownLatch(1);
    }

    @SneakyThrows
    public ChatChoice getChatChoice(){
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("等待OpenAI返回数据异常：{}", e);
            throw e;
        }
        return chatChoice;
    }

    @Override
    public void onClosed(EventSource eventSource) {
        log.info("OpenAI关闭sse连接...");
    }

    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        if(Objects.isNull(response)){
            sseEmitter.complete();
            log.error("OpenAI  sse连接异常:{}", t);
            eventSource.cancel();
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", body.string(), t);
        } else {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", response, t);
        }
        eventSource.cancel();
    }
}