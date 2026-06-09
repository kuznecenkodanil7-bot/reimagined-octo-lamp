package com.example.advancedchattabs.chat;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class MessageMergeManager {
    public <T> ChatHistory.Entry<T> mergeOrAppend(ChatHistory<T> history, ChatMessage<T> incoming, Duration maximumInterval) {
        Objects.requireNonNull(history, "history");
        Objects.requireNonNull(incoming, "incoming");
        Objects.requireNonNull(maximumInterval, "maximumInterval");

        ChatHistory.Entry<T> last = history.last();
        if (last != null
                && last.message().plainText().equals(incoming.plainText())
                && !incoming.receivedAt().isBefore(last.lastSeen())
                && Duration.between(last.lastSeen(), incoming.receivedAt()).compareTo(maximumInterval) <= 0) {
            ChatHistory.Entry<T> merged = new ChatHistory.Entry<>(
                    incoming,
                    last.repeatCount() + 1,
                    last.firstSeen(),
                    incoming.receivedAt()
            );
            history.replaceLast(merged);
            return merged;
        }

        history.add(incoming);
        return history.last();
    }

    public boolean canMerge(String previousText, Instant previousTime, String nextText, Instant nextTime, Duration maximumInterval) {
        return Objects.equals(previousText, nextText)
                && previousTime != null
                && nextTime != null
                && !nextTime.isBefore(previousTime)
                && Duration.between(previousTime, nextTime).compareTo(maximumInterval) <= 0;
    }
}
