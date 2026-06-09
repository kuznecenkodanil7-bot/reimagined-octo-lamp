package com.example.advancedchattabs.chat;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MessageMergeManagerTest {
    @Test
    void mergesConsecutiveMessagesWithinInterval() {
        Instant firstTime = Instant.parse("2026-01-01T00:00:00Z");
        ChatHistory<String> history = new ChatHistory<>(100);
        MessageMergeManager manager = new MessageMergeManager();
        ChatMessage<String> first = new ChatMessage<>(UUID.randomUUID(), "x", "Игрок вошёл на сервер", "", "", ChatMessageType.SERVER, TrustStatus.SYSTEM, firstTime);
        ChatMessage<String> second = new ChatMessage<>(UUID.randomUUID(), "x", "Игрок вошёл на сервер", "", "", ChatMessageType.SERVER, TrustStatus.SYSTEM, firstTime.plusSeconds(5));

        manager.mergeOrAppend(history, first, Duration.ofSeconds(10));
        ChatHistory.Entry<String> result = manager.mergeOrAppend(history, second, Duration.ofSeconds(10));
        assertEquals(1, history.snapshot().size());
        assertEquals(2, result.repeatCount());
        assertEquals(firstTime, result.firstSeen());
        assertEquals(firstTime.plusSeconds(5), result.lastSeen());
    }

    @Test
    void doesNotMergeAfterInterval() {
        MessageMergeManager manager = new MessageMergeManager();
        assertFalse(manager.canMerge("a", Instant.EPOCH, "a", Instant.EPOCH.plusSeconds(11), Duration.ofSeconds(10)));
    }
}
