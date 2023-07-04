package com.ai.aigenerate.chat;

import com.unfbx.chatgpt.entity.chat.Functions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GptFunctionFactory {

    @Autowired
    private List<GptFunctionService> gptFunctionServices;

    private Map<String,GptFunctionService> gptFunctionServiceMap;

    private List<Functions> functions;

    @PostConstruct
    public void init(){
        gptFunctionServiceMap = new HashMap<>(gptFunctionServices.size());
        functions = new ArrayList<>(gptFunctionServices.size());
        for (GptFunctionService gptFunctionService : gptFunctionServices) {
            gptFunctionServiceMap.put(gptFunctionService.getFunction().getName(),gptFunctionService);
            functions.add(gptFunctionService.getFunction());
        }
    }

    public List<Functions> getFunctions(){
        return functions;
    }

    public List<Functions> getFunctionsByFunctionNameList(List<String> functionNameList){
        List<Functions> functions = new ArrayList<>(functionNameList.size());
        for (String functionName : functionNameList) {
            functions.add(getGptFunctionService(functionName).getFunction());
        }
        return functions;
    }

    public GptFunctionService getGptFunctionService(String functionName){
        return gptFunctionServiceMap.get(functionName);
    }
}
