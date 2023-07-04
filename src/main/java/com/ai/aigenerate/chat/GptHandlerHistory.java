package com.ai.aigenerate.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GptHandlerHistory {

    private String functionName;

    private String functionParam;

    private String result;
}
