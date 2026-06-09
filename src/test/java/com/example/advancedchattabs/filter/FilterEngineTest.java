package com.example.advancedchattabs.filter;

import com.example.advancedchattabs.chat.ChatMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilterEngineTest {
    private final FilterEngine engine = new FilterEngine();

    @Test
    void containsConditionMatches() {
        ChatFilter filter = filter("успешно вошли");
        assertTrue(engine.test(filter, ChatMessage.plain("Вы успешно вошли в аккаунт.")).matched());
    }

    @Test
    void excludingConditionRejectsMessage() {
        ChatFilter filter = filter("голосового чата");
        filter.setExcludeConditions(List.of("Simple Voice Chat"));
        FilterTestResult result = engine.test(filter, ChatMessage.plain("Для голосового чата установите Simple Voice Chat."));
        assertFalse(result.matched());
        assertEquals("Simple Voice Chat", result.excludedBy());
    }

    @Test
    void caseSensitivityIsHonored() {
        ChatFilter filter = filter("player123");
        filter.setCaseSensitive(true);
        assertFalse(engine.test(filter, ChatMessage.plain("[ЛС] Player123: привет")).matched());
        filter.setCaseSensitive(false);
        assertTrue(engine.test(filter, ChatMessage.plain("[ЛС] Player123: привет")).matched());
    }

    @Test
    void regularExpressionMatchesAndInvalidExpressionDoesNotCrash() {
        ChatFilter filter = filter("^Администратор.*событие$");
        filter.setUseRegularExpression(true);
        assertTrue(engine.test(filter, ChatMessage.plain("Администратор запустил событие")).matched());
        filter.setIncludeConditions(List.of("[unterminated"));
        FilterTestResult result = engine.test(filter, ChatMessage.plain("Продам алмазы"));
        assertFalse(result.matched());
        assertFalse(result.error().isBlank());
    }

    @Test
    void allModeRequiresEveryCondition() {
        ChatFilter filter = filter("Игрок");
        filter.setIncludeConditions(List.of("Игрок", "сервер"));
        filter.setLogicalMode(LogicalMode.ALL);
        assertTrue(engine.test(filter, ChatMessage.plain("Игрок вошёл на сервер")).matched());
        assertFalse(engine.test(filter, ChatMessage.plain("Игрок вышел")).matched());
    }

    private ChatFilter filter(String include) {
        ChatFilter filter = new ChatFilter("test");
        filter.setIncludeConditions(List.of(include));
        filter.setActions(List.of(FilterAction.addCurrent()));
        return filter;
    }
}
