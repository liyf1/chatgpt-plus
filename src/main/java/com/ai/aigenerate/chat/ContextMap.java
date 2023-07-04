package com.ai.aigenerate.chat;

import java.util.concurrent.ConcurrentHashMap;

public class ContextMap {

    private static ConcurrentHashMap<String, GptContext> gptApplicationMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, GptStreamContext> gptStreamContextConcurrentHashMap = new ConcurrentHashMap<>();

    public static void put(String key, GptContext value){
        gptApplicationMap.put(key,value);
    }

    public static void putStreamContext(String key, GptStreamContext value){
        gptStreamContextConcurrentHashMap.put(key,value);
    }

    //todo 是否安全
    public static void remove(String key){
        gptApplicationMap.remove(key);
    }

    public static GptContext get(String key){
        return gptApplicationMap.get(key);
    }

    public static void removeStreamContext(String key){
        gptApplicationMap.remove(key);
    }

    public static GptStreamContext getStreamContext(String key){
        return gptStreamContextConcurrentHashMap.get(key);
    }
}
