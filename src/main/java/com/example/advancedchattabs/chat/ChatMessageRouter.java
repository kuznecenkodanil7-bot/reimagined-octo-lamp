package com.example.advancedchattabs.chat;

import com.example.advancedchattabs.filter.ChatFilter;
import com.example.advancedchattabs.filter.FilterAction;
import com.example.advancedchattabs.filter.FilterActionType;
import com.example.advancedchattabs.filter.FilterEngine;
import com.example.advancedchattabs.filter.FilterTestResult;
import com.example.advancedchattabs.filter.FilterScope;
import com.example.advancedchattabs.tab.ChatTab;

import java.util.Comparator;
import java.util.List;

public final class ChatMessageRouter {
    private final FilterEngine filterEngine;

    public ChatMessageRouter(FilterEngine filterEngine) {
        this.filterEngine = filterEngine;
    }

    public RoutePlan route(ChatMessage<?> message, List<ChatTab> tabs) {
        RoutePlan plan = new RoutePlan();
        List<ChatTab> ordered = tabs.stream()
                .sorted(Comparator.comparing(ChatTab::isPinned).reversed().thenComparingInt(ChatTab::getOrder))
                .toList();

        ordered.stream().filter(ChatTab::isSystemTab).findFirst()
                .ifPresent(tab -> plan.destinationTabs().add(tab.getId()));

        for (ChatTab tab : ordered) {
            List<ChatFilter> filters = tab.getFilters().stream()
                    .sorted(Comparator.comparingInt(ChatFilter::getPriority).reversed())
                    .toList();
            for (ChatFilter filter : filters) {
                FilterTestResult result = filterEngine.test(filter, message);
                if (!result.matched()) {
                    continue;
                }
                if (result.actions().isEmpty()) {
                    if (filter.getScope() == FilterScope.ALL_TABS) {
                        ordered.forEach(candidate -> plan.destinationTabs().add(candidate.getId()));
                    } else {
                        plan.destinationTabs().add(tab.getId());
                    }
                }
                for (FilterAction action : result.actions()) {
                    plan.actions().add(action);
                    applyAction(plan, tab, action, filter.getScope(), ordered);
                    if (isVisualAction(action.getType())) {
                        if (filter.getScope() == FilterScope.ALL_TABS) {
                            ordered.forEach(candidate -> plan.addTabAction(candidate.getId(), action));
                        } else {
                            plan.addTabAction(tab.getId(), action);
                        }
                    }
                }
                if (filter.isStopAfterMatch()) {
                    break;
                }
            }
        }

        if (plan.globallyHidden()) {
            plan.destinationTabs().clear();
        } else {
            plan.destinationTabs().removeAll(plan.hiddenInTabs());
        }
        return plan;
    }


    private boolean isVisualAction(FilterActionType type) {
        return type == FilterActionType.SET_TEXT_COLOR
                || type == FilterActionType.SET_BACKGROUND_COLOR
                || type == FilterActionType.SET_BACKGROUND_ALPHA
                || type == FilterActionType.ADD_ICON
                || type == FilterActionType.MARK_IMPORTANT;
    }

    private void applyAction(RoutePlan plan, ChatTab owner, FilterAction action, FilterScope scope, List<ChatTab> allTabs) {
        FilterActionType type = action.getType();
        if (type == null) {
            return;
        }
        switch (type) {
            case ADD_TO_CURRENT_TAB -> {
                if (scope == FilterScope.ALL_TABS) allTabs.forEach(tab -> plan.destinationTabs().add(tab.getId()));
                else plan.destinationTabs().add(owner.getId());
            }
            case ADD_TO_TAB -> {
                if (action.getTargetTabId() != null) {
                    plan.destinationTabs().add(action.getTargetTabId());
                }
            }
            case HIDE_MESSAGE -> {
                if (action.isGlobalHide()) {
                    plan.setGloballyHidden(true);
                } else if (scope == FilterScope.ALL_TABS) {
                    allTabs.forEach(tab -> plan.hiddenInTabs().add(tab.getId()));
                } else {
                    plan.hiddenInTabs().add(owner.getId());
                }
            }
            case INCREMENT_UNREAD -> {
                if (scope == FilterScope.ALL_TABS) allTabs.forEach(tab -> plan.unreadTabs().add(tab.getId()));
                else plan.unreadTabs().add(owner.getId());
            }
            default -> {
                // Visual, audio and notification actions are executed after routing.
            }
        }
    }
}
