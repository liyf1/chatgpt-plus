package com.ai.aigenerate.chat.tool;

import com.ai.aigenerate.config.GptConfig;
import com.ai.aigenerate.constant.MjConstant;
import com.ai.aigenerate.model.request.mj.CreateTaskRequest;
import com.ai.aigenerate.model.response.mj.MjTaskResponse;
import com.ai.aigenerate.model.response.mj.QueryTaskResponse;
import com.ai.aigenerate.utils.HttpClientUtils;
import com.ai.aigenerate.utils.MjTaskDelayed;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.DelayQueue;

@Service
public class MjService {

    @Autowired
    private GptConfig gptConfig;

    private DelayQueue<MjTaskDelayed> taskQueue = new DelayQueue<>();

    public MjTaskResponse createTextTask(String prompt){
        CreateTaskRequest createTaskRequest = new CreateTaskRequest();
        createTaskRequest.setPrompt(prompt);
        JSONObject jsonObject = HttpClientUtils.httpPost(gptConfig.getMjServiceUrl()+ MjConstant.IMAGE_URL, JSON.toJSONString(createTaskRequest));
        return JSONObject.toJavaObject(jsonObject, MjTaskResponse.class);
    }

    public QueryTaskResponse getTask(String taskId){
        if (StringUtils.isBlank(taskId)){
            return null;
        }
        JSONObject jsonObject = HttpClientUtils.httpGet(gptConfig.getMjServiceUrl()+ MjConstant.QUERY_TASK_URL+taskId+ MjConstant.QUERY_TASK_URL_FETCH);
        return JSONObject.toJavaObject(jsonObject, QueryTaskResponse.class);
    }

    @SneakyThrows
    public QueryTaskResponse addTask(String prompt){
        DelayQueue<MjTaskDelayed> taskQueue = new DelayQueue<>();
        MjTaskResponse mjTaskResponse = createTextTask(prompt);
        MjTaskDelayed mjTaskDelayed = new MjTaskDelayed(mjTaskResponse.getResult(),  gptConfig.getMjServiceWaitTime());
        int maxAttempts = 5; // 最大重试次数
        int attempts = 0;
        while (attempts < maxAttempts) {
            new Thread(()->{
                taskQueue.offer(mjTaskDelayed);
            }).start();

            MjTaskDelayed delayed = taskQueue.take();
            QueryTaskResponse queryTaskResponse = getTask(delayed.getTaskId());

            if (MjConstant.SUCCESS.equals(queryTaskResponse.status)) {
                return queryTaskResponse;
            }

            mjTaskDelayed.resetDelay(gptConfig.getMjServiceWaitTime());
            attempts++;
        }

        // 达到最大等待次数，返回默认响应或执行其他处理
        return getTask(mjTaskResponse.getResult());
    }
}
