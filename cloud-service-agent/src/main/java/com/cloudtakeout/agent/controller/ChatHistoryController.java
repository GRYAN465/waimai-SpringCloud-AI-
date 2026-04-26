package com.cloudtakeout.agent.controller;

import com.cloudtakeout.agent.entity.vo.MessageVO;
import com.cloudtakeout.agent.repository.ChatHistoryRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/agent/history")
@CrossOrigin
public class ChatHistoryController {
    private final ChatHistoryRepository chatHistoryRepository;
    private final ChatMemory chatMemory;

    public ChatHistoryController(ChatHistoryRepository chatHistoryRepository, ChatMemory chatMemory) {
        this.chatHistoryRepository = chatHistoryRepository;
        this.chatMemory = chatMemory;
    }

    @GetMapping("/{type}")
    public List<String> getChatIds(@PathVariable String type) {
        return chatHistoryRepository.getConversationId(type);
    }

    @GetMapping("/{type}/{conversationId}")
    public List<MessageVO> getChatHistory(@PathVariable String type, @PathVariable String conversationId) {
        List<Message> messages = chatMemory.get(conversationId, Integer.MAX_VALUE);
        return messages.stream().map(MessageVO::new).toList();
    }

    @GetMapping("/save")
    public void saveChatId(@RequestParam String type, @RequestParam String conversationId) {
        chatHistoryRepository.save(type, conversationId);
    }
}
