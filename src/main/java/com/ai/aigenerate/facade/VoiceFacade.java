package com.ai.aigenerate.facade;

import com.ai.aigenerate.chat.ChatService;
import com.ai.aigenerate.constant.VoiceContent;
import com.ai.aigenerate.model.request.chat.ChatRequest;
import com.ai.aigenerate.model.request.chat.ChatVoiceRequest;
import com.ai.aigenerate.model.response.chat.ChatResponse;
import com.ai.aigenerate.model.response.chat.VoiceResponse;
import com.alibaba.fastjson2.JSON;
import com.unfbx.chatgpt.entity.chat.Message;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RequestMapping("/voice")
@RestController
public class VoiceFacade {


    @Autowired
    private ChatService chatService;

    @PostMapping("/upload-audio")
    public VoiceResponse handleAudioUpload(@RequestParam("audio") MultipartFile audioFile, @RequestParam("chatHistory") String chatHistoryStr, HttpServletResponse response) throws IOException {
        // 在这里处理上传的音频文件
        List<ChatVoiceRequest> chatVoiceRequests =  JSON.parseArray(chatHistoryStr, ChatVoiceRequest.class);
        // 可以将音频保存到服务器上的某个位置，或者执行其他操作
        File file = new File(VoiceContent.ASR_PATH);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        bufferedOutputStream.write(audioFile.getBytes());
        String questionAsr = chatService.speechToTextTranslations(file);
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setPrompt(questionAsr);
        List<Message> messages = new ArrayList<>();
        for (ChatVoiceRequest chatVoiceRequest : chatVoiceRequests) {
            Message message = new Message();
            message.setRole(Message.Role.USER.getName());
            message.setContent(chatVoiceRequest.getQuestion());
            messages.add(message);
            Message message1 = new Message();
            message1.setRole(Message.Role.ASSISTANT.getName());
            message1.setContent(chatVoiceRequest.getAnswer());
            messages.add(message1);
        }
        chatRequest.setMessages(messages);
        ChatResponse chatResponse = chatService.chat(chatRequest);
        VoiceResponse voiceResponse = new VoiceResponse();
        voiceResponse.setQuestionAsr(questionAsr);
        voiceResponse.setAnswerAsr(chatResponse.getResult());
        File answerTts = chatService.textToSpeed(chatResponse.getResult());
        byte[] audioBytes = Files.readAllBytes(Paths.get(answerTts.getPath()));
        // 将音频字节编码为Base64字符串
        String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);
        voiceResponse.setAudio(audioBase64);
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        return voiceResponse;
    }
}
