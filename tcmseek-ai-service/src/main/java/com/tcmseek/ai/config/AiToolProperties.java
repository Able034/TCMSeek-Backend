package com.tcmseek.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tcmseek.ai.tools")
public class AiToolProperties {

    private int queryLimit = 1000;

    private int answerItemLimit = 8;

    public int getQueryLimit() {
        return queryLimit;
    }

    public void setQueryLimit(int queryLimit) {
        this.queryLimit = queryLimit;
    }

    public int getAnswerItemLimit() {
        return answerItemLimit;
    }

    public void setAnswerItemLimit(int answerItemLimit) {
        this.answerItemLimit = answerItemLimit;
    }
}
