package com.cloudtakeout.agent.repository;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository {
    private final Map<String, Set<String>> store = new ConcurrentHashMap<>();

    @Override
    public void save(String type, String conversationId) {
        store.computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet()).add(conversationId);
    }

    @Override
    public List<String> getConversationId(String type) {
        return store.getOrDefault(type, Set.of())
                .stream()
                .sorted(Comparator.reverseOrder())
                .toList();
    }
}
