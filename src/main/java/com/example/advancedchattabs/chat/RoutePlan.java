package com.example.advancedchattabs.chat;

import com.example.advancedchattabs.filter.FilterAction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class RoutePlan {
    private final Set<UUID> destinationTabs = new LinkedHashSet<>();
    private final List<FilterAction> actions = new ArrayList<>();
    private final Map<UUID, List<FilterAction>> tabActions = new LinkedHashMap<>();
    private final Set<UUID> hiddenInTabs = new LinkedHashSet<>();
    private final Set<UUID> unreadTabs = new LinkedHashSet<>();
    private boolean globallyHidden;

    public Set<UUID> destinationTabs() { return destinationTabs; }
    public List<FilterAction> actions() { return actions; }
    public Set<UUID> hiddenInTabs() { return hiddenInTabs; }
    public Set<UUID> unreadTabs() { return unreadTabs; }
    public boolean globallyHidden() { return globallyHidden; }
    public void setGloballyHidden(boolean value) { globallyHidden = value; }

    public void addTabAction(UUID tabId, FilterAction action) {
        if (tabId != null && action != null) {
            tabActions.computeIfAbsent(tabId, ignored -> new ArrayList<>()).add(action);
        }
    }

    public List<FilterAction> actionsFor(UUID tabId) {
        return List.copyOf(tabActions.getOrDefault(tabId, List.of()));
    }
}
