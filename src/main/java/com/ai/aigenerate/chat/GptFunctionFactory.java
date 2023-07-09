package com.ai.aigenerate.chat;

import com.ai.aigenerate.model.request.chat.FunctionDefinition;
import com.ai.aigenerate.utils.HttpClientUtils;
import com.ai.aigenerate.utils.MdcUtil;
import com.alibaba.fastjson.JSON;
import com.unfbx.chatgpt.entity.chat.Functions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GptFunctionFactory {

    @Autowired
    private List<GptFunctionService> gptFunctionServices;

    private Map<String,GptFunctionService> gptFunctionServiceMap;

    private Map<String,Map<String,GptFunctionService>> tempReqFunctionServiceMap = new ConcurrentHashMap<>();

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

    public List<GptFunctionService> getGptFunctionServices(List<FunctionDefinition> functionDefinitions){
        List<GptFunctionService> gptFunctionServices = new ArrayList<>(functionDefinitions.size());
        Map<String,GptFunctionService> tempFunctionServiceMap = new HashMap<>(functionDefinitions.size());
        for (FunctionDefinition functionDefinition : functionDefinitions) {
            GptFunctionService tempService = new AbstractGptFunctionHandler<>() {
                @Override
                public String doHandle(String paramJson) {
                    if ("post".equals(functionDefinition.getFunctionCurl().getType())) {
                        return HttpClientUtils.httpPost(functionDefinition.getFunctionCurl().getUrl(), paramJson).toJSONString();
                    }else {
                        Map param = JSON.parseObject(paramJson, Map.class);
                        return HttpClientUtils.httpGet(functionDefinition.getFunctionCurl().getUrl(), param).toJSONString();
                    }
                }
                @Override
                public Functions getFunction() {
                    return functionDefinition.getFunctions();
                }
            };
            tempFunctionServiceMap.put(functionDefinition.getFunctions().getName(),tempService);
            gptFunctionServices.add(tempService);
        }
        tempReqFunctionServiceMap.put(MdcUtil.getTraceId(),tempFunctionServiceMap);
        return gptFunctionServices;
    }

    public GptFunctionService getGptFunctionServiceByTraceId(String functionName){
        return tempReqFunctionServiceMap.get(MdcUtil.getTraceId()).get(functionName);
    }
}
