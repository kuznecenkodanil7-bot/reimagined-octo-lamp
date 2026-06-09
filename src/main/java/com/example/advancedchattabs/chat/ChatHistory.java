package com.example.advancedchattabs.chat;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class ChatHistory<T> {
    public record Entry<T>(ChatMessage<T> message, int repeatCount, Instant firstSeen, Instant lastSeen) {}

    private final Deque<Entry<T>> entries = new ArrayDeque<>();
    private int maximumSize;

    public ChatHistory(int maximumSize) {
        this.maximumSize = Math.max(1, maximumSize);
    }

    public synchronized void add(ChatMessage<T> message) {
        entries.addLast(new Entry<>(message, 1, message.receivedAt(), message.receivedAt()));
        trim();
    }

    public synchronized void addEntry(Entry<T> entry) {
        if (entry != null) {
            entries.addLast(entry);
            trim();
        }
    }

    public synchronized void replaceLast(Entry<T> entry) {
        if (!entries.isEmpty()) {
            entries.removeLast();
        }
        entries.addLast(entry);
        trim();
    }

    public synchronized Entry<T> last() {
        return entries.peekLast();
    }

    public synchronized List<Entry<T>> snapshot() {
        return List.copyOf(new ArrayList<>(entries));
    }

    public synchronized int size() { return entries.size(); }

    public synchronized void clear() {
        entries.clear();
    }

    public synchronized void setMaximumSize(int maximumSize) {
        this.maximumSize = Math.max(1, maximumSize);
        trim();
    }

    private void trim() {
        while (entries.size() > maximumSize) {
            entries.removeFirst();
        }
    }
}
