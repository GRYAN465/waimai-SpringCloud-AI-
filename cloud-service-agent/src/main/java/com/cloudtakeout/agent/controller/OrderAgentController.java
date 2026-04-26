package com.cloudtakeout.agent.controller;

import com.cloudtakeout.agent.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/agent")
public class OrderAgentController {

    private final ChatClient orderAgentChatClient;
    private final ChatHistoryRepository chatHistoryRepository;

    @GetMapping(value = "/chat", produces = "text/plain;charset=utf-8")
    public String chat(@RequestParam String prompt, @RequestParam String conversationId) {
        chatHistoryRepository.save("order-agent", conversationId);
        return orderAgentChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .call()
                .content();
    }
}
