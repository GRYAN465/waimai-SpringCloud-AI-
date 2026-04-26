package com.cloudtakeout.agent.repository;

import java.util.List;

public interface ChatHistoryRepository {
    void save(String type, String conversationId);

    List<String> getConversationId(String type);
}
