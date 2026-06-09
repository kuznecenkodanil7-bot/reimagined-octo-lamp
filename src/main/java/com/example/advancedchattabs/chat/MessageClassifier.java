package com.example.advancedchattabs.chat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageClassifier {
    private static final Pattern PRIVATE_MESSAGE = Pattern.compile("^(?:\\[ЛС]|\\[PM]|From |To )\\s*([^:>]+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern PLAYER_CHAT = Pattern.compile("^(?:<([^>]+)>|\\[?([^] :]+)]?\\s*:)\\s*.+");

    public ChatMessageType classify(String plainText, boolean signed, boolean systemHint) {
        String text = plainText == null ? "" : plainText.trim();
        if (systemHint) {
            return text.startsWith("/") ? ChatMessageType.COMMAND : ChatMessageType.SYSTEM;
        }
        if (signed || PLAYER_CHAT.matcher(text).matches() || PRIVATE_MESSAGE.matcher(text).find()) {
            return ChatMessageType.PLAYER;
        }
        if (text.startsWith("/")) {
            return ChatMessageType.COMMAND;
        }
        return ChatMessageType.SERVER;
    }

    public String sender(String plainText) {
        String text = plainText == null ? "" : plainText.trim();
        Matcher privateMatcher = PRIVATE_MESSAGE.matcher(text);
        if (privateMatcher.find()) {
            return privateMatcher.group(1).trim();
        }
        Matcher chatMatcher = PLAYER_CHAT.matcher(text);
        if (chatMatcher.matches()) {
            String value = chatMatcher.group(1) != null ? chatMatcher.group(1) : chatMatcher.group(2);
            return value == null ? "" : value.trim();
        }
        return "";
    }
}
