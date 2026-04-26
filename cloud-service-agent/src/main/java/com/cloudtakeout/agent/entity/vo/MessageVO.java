package com.cloudtakeout.agent.entity.vo;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;

public class MessageVO {
    private String role;
    private String content;

    public MessageVO(Message message) {
        if (message instanceof UserMessage) {
            this.role = "user";
        } else if (message instanceof AssistantMessage) {
            this.role = "assistant";
        } else if (message instanceof SystemMessage) {
            this.role = "system";
        } else if (message instanceof ToolResponseMessage) {
            this.role = "tool";
        } else {
            this.role = "unknown";
        }
        this.content = message.getText();
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}
