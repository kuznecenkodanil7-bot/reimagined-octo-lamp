package com.example.advancedchattabs.chat;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ChatMessage<T> {
    private final UUID id;
    private final T richText;
    private final String plainText;
    private final String hoverText;
    private final String sender;
    private final ChatMessageType type;
    private final TrustStatus trustStatus;
    private final Instant receivedAt;

    public ChatMessage(
            UUID id,
            T richText,
            String plainText,
            String hoverText,
            String sender,
            ChatMessageType type,
            TrustStatus trustStatus,
            Instant receivedAt
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.richText = richText;
        this.plainText = Objects.requireNonNullElse(plainText, "");
        this.hoverText = Objects.requireNonNullElse(hoverText, "");
        this.sender = Objects.requireNonNullElse(sender, "");
        this.type = Objects.requireNonNullElse(type, ChatMessageType.UNKNOWN);
        this.trustStatus = Objects.requireNonNullElse(trustStatus, TrustStatus.UNSIGNED);
        this.receivedAt = Objects.requireNonNullElseGet(receivedAt, Instant::now);
    }

    public static ChatMessage<String> plain(String text) {
        return new ChatMessage<>(UUID.randomUUID(), text, text, "", "", ChatMessageType.UNKNOWN, TrustStatus.UNSIGNED, Instant.now());
    }

    public UUID id() { return id; }
    public T richText() { return richText; }
    public String plainText() { return plainText; }
    public String hoverText() { return hoverText; }
    public String sender() { return sender; }
    public ChatMessageType type() { return type; }
    public TrustStatus trustStatus() { return trustStatus; }
    public Instant receivedAt() { return receivedAt; }

    public String searchableText(boolean includeHover, boolean includeSender) {
        StringBuilder value = new StringBuilder(plainText);
        if (includeHover && !hoverText.isBlank()) {
            value.append('\n').append(hoverText);
        }
        if (includeSender && !sender.isBlank()) {
            value.append('\n').append(sender);
        }
        return value.toString();
    }
}
