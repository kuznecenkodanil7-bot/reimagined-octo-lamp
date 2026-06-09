package com.example.advancedchattabs.chat;

import com.example.advancedchattabs.filter.ChatFilter;
import com.example.advancedchattabs.filter.FilterAction;
import com.example.advancedchattabs.filter.FilterEngine;
import com.example.advancedchattabs.tab.ChatTab;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageRouterTest {
    @Test
    void routesMainAndMatchingCustomTab() {
        ChatTab main = new ChatTab("Server", true, 0);
        ChatTab trade = new ChatTab("Trade", false, 1);
        ChatFilter filter = new ChatFilter("diamonds");
        filter.setIncludeConditions(List.of("Продам"));
        filter.setActions(List.of(FilterAction.addCurrent()));
        trade.getFilters().add(filter);

        RoutePlan plan = new ChatMessageRouter(new FilterEngine()).route(ChatMessage.plain("Продам алмазы"), List.of(main, trade));
        assertTrue(plan.destinationTabs().contains(main.getId()));
        assertTrue(plan.destinationTabs().contains(trade.getId()));
    }

    @Test
    void globalHideRemovesEveryDestination() {
        ChatTab main = new ChatTab("Server", true, 0);
        ChatTab hidden = new ChatTab("Hidden", false, 1);
        ChatFilter filter = new ChatFilter("spam");
        filter.setIncludeConditions(List.of("Simple Voice Chat"));
        filter.setActions(List.of(FilterAction.hide(true)));
        hidden.getFilters().add(filter);

        RoutePlan plan = new ChatMessageRouter(new FilterEngine()).route(ChatMessage.plain("Для голосового чата установите Simple Voice Chat."), List.of(main, hidden));
        assertTrue(plan.globallyHidden());
        assertTrue(plan.destinationTabs().isEmpty());
    }
}
