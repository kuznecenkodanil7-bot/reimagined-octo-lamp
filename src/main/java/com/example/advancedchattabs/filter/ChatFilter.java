package com.example.advancedchattabs.filter;

import com.example.advancedchattabs.chat.ChatMessageType;
import com.example.advancedchattabs.chat.TrustStatus;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ChatFilter {
    private UUID id = UUID.randomUUID();
    private String name = "Filter";
    private boolean enabled = true;
    private int priority;
    private List<String> includeConditions = new ArrayList<>();
    private List<String> excludeConditions = new ArrayList<>();
    private MatchMode matchMode = MatchMode.CONTAINS;
    private LogicalMode logicalMode = LogicalMode.ANY;
    private boolean caseSensitive;
    private boolean useRegularExpression;
    private boolean inspectHover;
    private boolean plainTextOnly = true;
    private boolean inspectSender;
    private Set<ChatMessageType> messageTypes = EnumSet.allOf(ChatMessageType.class);
    private Set<TrustStatus> trustStatuses = EnumSet.allOf(TrustStatus.class);
    private boolean stopAfterMatch;
    private FilterScope scope = FilterScope.CURRENT_TAB;
    private List<FilterAction> actions = new ArrayList<>(List.of(FilterAction.addCurrent()));

    public ChatFilter() {}

    public ChatFilter(String name) {
        this.name = name;
    }

    public void normalize() {
        if (id == null) id = UUID.randomUUID();
        if (name == null || name.isBlank()) name = "Filter";
        if (includeConditions == null) includeConditions = new ArrayList<>();
        if (excludeConditions == null) excludeConditions = new ArrayList<>();
        if (matchMode == null) matchMode = MatchMode.CONTAINS;
        if (logicalMode == null) logicalMode = LogicalMode.ANY;
        if (messageTypes == null) messageTypes = EnumSet.allOf(ChatMessageType.class);
        if (trustStatuses == null) trustStatuses = EnumSet.allOf(TrustStatus.class);
        if (scope == null) scope = FilterScope.CURRENT_TAB;
        if (actions == null) actions = new ArrayList<>();
        actions.removeIf(action -> action == null);
    }

    public ChatFilter copy() {
        normalize();
        ChatFilter copy = new ChatFilter();
        copy.id = UUID.randomUUID();
        copy.name = name + " copy";
        copy.enabled = enabled;
        copy.priority = priority;
        copy.includeConditions = new ArrayList<>(includeConditions);
        copy.excludeConditions = new ArrayList<>(excludeConditions);
        copy.matchMode = matchMode;
        copy.logicalMode = logicalMode;
        copy.caseSensitive = caseSensitive;
        copy.useRegularExpression = useRegularExpression;
        copy.inspectHover = inspectHover;
        copy.plainTextOnly = plainTextOnly;
        copy.inspectSender = inspectSender;
        copy.messageTypes = messageTypes.isEmpty() ? EnumSet.noneOf(ChatMessageType.class) : EnumSet.copyOf(messageTypes);
        copy.trustStatuses = trustStatuses.isEmpty() ? EnumSet.noneOf(TrustStatus.class) : EnumSet.copyOf(trustStatuses);
        copy.stopAfterMatch = stopAfterMatch;
        copy.scope = scope;
        copy.actions = new ArrayList<>();
        for (FilterAction action : actions) {
            FilterAction cloned = new FilterAction(action.getType());
            cloned.setTargetTabId(action.getTargetTabId());
            cloned.setTargetWindowId(action.getTargetWindowId());
            cloned.setColor(action.getColor());
            cloned.setAlpha(action.getAlpha());
            cloned.setSoundId(action.getSoundId());
            cloned.setVolume(action.getVolume());
            cloned.setPitch(action.getPitch());
            cloned.setCooldownMillis(action.getCooldownMillis());
            cloned.setIcon(action.getIcon());
            cloned.setGlobalHide(action.isGlobalHide());
            copy.actions.add(cloned);
        }
        return copy;
    }

    public UUID getId() { normalize(); return id; }
    public String getName() { normalize(); return name; }
    public void setName(String name) { this.name = name; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public List<String> getIncludeConditions() { normalize(); return includeConditions; }
    public void setIncludeConditions(List<String> includeConditions) { this.includeConditions = includeConditions == null ? new ArrayList<>() : new ArrayList<>(includeConditions); }
    public List<String> getExcludeConditions() { normalize(); return excludeConditions; }
    public void setExcludeConditions(List<String> excludeConditions) { this.excludeConditions = excludeConditions == null ? new ArrayList<>() : new ArrayList<>(excludeConditions); }
    public MatchMode getMatchMode() { normalize(); return matchMode; }
    public void setMatchMode(MatchMode matchMode) { this.matchMode = matchMode; }
    public LogicalMode getLogicalMode() { normalize(); return logicalMode; }
    public void setLogicalMode(LogicalMode logicalMode) { this.logicalMode = logicalMode; }
    public boolean isCaseSensitive() { return caseSensitive; }
    public void setCaseSensitive(boolean caseSensitive) { this.caseSensitive = caseSensitive; }
    public boolean isUseRegularExpression() { return useRegularExpression; }
    public void setUseRegularExpression(boolean useRegularExpression) { this.useRegularExpression = useRegularExpression; }
    public boolean isInspectHover() { return inspectHover; }
    public void setInspectHover(boolean inspectHover) { this.inspectHover = inspectHover; }
    public boolean isPlainTextOnly() { return plainTextOnly; }
    public void setPlainTextOnly(boolean plainTextOnly) { this.plainTextOnly = plainTextOnly; }
    public boolean isInspectSender() { return inspectSender; }
    public void setInspectSender(boolean inspectSender) { this.inspectSender = inspectSender; }
    public Set<ChatMessageType> getMessageTypes() { normalize(); return messageTypes; }
    public void setMessageTypes(Set<ChatMessageType> messageTypes) {
        this.messageTypes = messageTypes == null ? EnumSet.allOf(ChatMessageType.class)
                : messageTypes.isEmpty() ? EnumSet.noneOf(ChatMessageType.class) : EnumSet.copyOf(messageTypes);
    }
    public Set<TrustStatus> getTrustStatuses() { normalize(); return trustStatuses; }
    public void setTrustStatuses(Set<TrustStatus> trustStatuses) {
        this.trustStatuses = trustStatuses == null ? EnumSet.allOf(TrustStatus.class)
                : trustStatuses.isEmpty() ? EnumSet.noneOf(TrustStatus.class) : EnumSet.copyOf(trustStatuses);
    }
    public boolean isStopAfterMatch() { return stopAfterMatch; }
    public void setStopAfterMatch(boolean stopAfterMatch) { this.stopAfterMatch = stopAfterMatch; }
    public FilterScope getScope() { normalize(); return scope; }
    public void setScope(FilterScope scope) { this.scope = scope; }
    public List<FilterAction> getActions() { normalize(); return actions; }
    public void setActions(List<FilterAction> actions) { this.actions = actions == null ? new ArrayList<>() : new ArrayList<>(actions); }
}
