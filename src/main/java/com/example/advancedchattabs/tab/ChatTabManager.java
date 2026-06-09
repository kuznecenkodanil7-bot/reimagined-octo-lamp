package com.example.advancedchattabs.tab;

import com.example.advancedchattabs.chat.ChatHistory;
import com.example.advancedchattabs.chat.ChatMessage;
import com.example.advancedchattabs.chat.ChatMessageRouter;
import com.example.advancedchattabs.chat.ChatMessageType;
import com.example.advancedchattabs.chat.MessageClassifier;
import com.example.advancedchattabs.chat.MessageMergeManager;
import com.example.advancedchattabs.chat.RoutePlan;
import com.example.advancedchattabs.chat.TrustStatus;
import com.example.advancedchattabs.chat.VanillaChatPayload;
import com.example.advancedchattabs.config.ConfigManager;
import com.example.advancedchattabs.config.HistoryStore;
import com.example.advancedchattabs.config.ServerConfig;
import com.example.advancedchattabs.filter.FilterAction;
import com.example.advancedchattabs.filter.FilterActionType;
import com.example.advancedchattabs.util.ServerIdentity;
import com.example.advancedchattabs.window.ChatWindow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ChatTabManager {
    private final MinecraftClient client;
    private final ConfigManager configManager;
    private final ChatMessageRouter router;
    private final MessageClassifier classifier = new MessageClassifier();
    private final MessageMergeManager mergeManager = new MessageMergeManager();
    private final HistoryStore historyStore;
    private final Logger logger;
    private final ThreadLocal<Boolean> internalCall = ThreadLocal.withInitial(() -> false);
    private final ThreadLocal<Boolean> internalOverlay = ThreadLocal.withInitial(() -> false);
    private final Map<String, Long> soundCooldowns = new HashMap<>();
    private ServerIdentity identity = new ServerIdentity("disconnected", "Chat");
    private ServerConfig serverConfig;
    private boolean initialized;

    public ChatTabManager(MinecraftClient client, ConfigManager configManager, ChatMessageRouter router, Logger logger) {
        this.client = client;
        this.configManager = configManager;
        this.router = router;
        this.logger = logger;
        this.historyStore = new HistoryStore(configManager.historiesDirectory(), configManager.gson(), logger);
        this.serverConfig = configManager.server(identity.key(), identity.displayName());
        this.historyStore.load(identity.key(), this.serverConfig.getTabs(), configManager.global().getDefaultTabSettings());
    }

    public void tick() {
        ServerIdentity current = ServerIdentity.current(client);
        if (!current.key().equals(identity.key())) {
            switchProfile(current);
        }
        if (!initialized && client.inGameHud != null) {
            initializeStates(client.inGameHud.getChatHud());
        }
    }

    public boolean interceptAdd(ChatHud hud, Text text, MessageSignatureData signature, MessageIndicator indicator) {
        if (Boolean.TRUE.equals(internalCall.get())) {
            return false;
        }
        routeMessage(hud, text, signature, indicator, null);
        // The routed copies have already been inserted into the appropriate vanilla ChatState objects.
        return true;
    }

    public boolean interceptOverlayMessage(Text text) {
        if (Boolean.TRUE.equals(internalOverlay.get()) || client.inGameHud == null) {
            return false;
        }
        RoutePlan plan = routeMessage(client.inGameHud.getChatHud(), text, null, null, ChatMessageType.ACTION_BAR);
        // Keep the vanilla action-bar display unless a filter explicitly requested global hiding.
        return plan.globallyHidden();
    }

    private RoutePlan routeMessage(
            ChatHud hud,
            Text text,
            MessageSignatureData signature,
            MessageIndicator indicator,
            ChatMessageType forcedType
    ) {
        tick();
        initializeStates(hud);

        String plain = text == null ? "" : text.getString();
        boolean signed = signature != null;
        boolean system = signature == null && indicator == null;
        TrustStatus trust = system ? TrustStatus.SYSTEM : signed ? (indicator == null ? TrustStatus.VERIFIED : TrustStatus.MODIFIED) : TrustStatus.UNSIGNED;
        Text safeText = text == null ? Text.empty() : text;
        VanillaChatPayload payload = new VanillaChatPayload(safeText.copy(), signature, indicator);
        ChatMessage<VanillaChatPayload> message = new ChatMessage<>(
                UUID.randomUUID(),
                payload,
                plain,
                extractHoverText(safeText),
                classifier.sender(plain),
                forcedType == null ? classifier.classify(plain, signed, system) : forcedType,
                trust,
                Instant.now()
        );

        RoutePlan plan = router.route(message, tabs());
        if (!plan.globallyHidden()) {
            for (FilterAction action : plan.actions()) {
                if (action.getType() == FilterActionType.COPY_TO_WINDOW && action.getTargetWindowId() != null) {
                    configManager.windows().getWindows().stream()
                            .filter(window -> action.getTargetWindowId().equals(window.getId()))
                            .map(ChatWindow::getActiveTabId)
                            .filter(java.util.Objects::nonNull)
                            .findFirst()
                            .ifPresent(plan.destinationTabs()::add);
                }
            }
        }
        ChatTab active = activeTab();
        active.setVanillaState(hud.toChatState());

        for (UUID destination : plan.destinationTabs()) {
            find(destination).ifPresent(tab -> addToTab(hud, tab, message, plan.actionsFor(destination)));
        }
        restore(hud, active);
        active.setUnreadCount(0);
        for (UUID unreadTab : plan.unreadTabs()) {
            find(unreadTab).ifPresent(ChatTab::incrementUnread);
        }
        executeActions(plan.actions(), message);
        configManager.requestSave();
        historyStore.requestSave(identity.key(), tabs(), configManager.global().getDefaultTabSettings());
        return plan;
    }

    private String extractHoverText(Text root) {
        StringBuilder output = new StringBuilder();
        collectHoverText(root, output);
        return output.toString();
    }

    private void collectHoverText(Text node, StringBuilder output) {
        if (node == null) return;
        Object hoverEvent = node.getStyle().getHoverEvent();
        if (hoverEvent != null) {
            if (!output.isEmpty()) output.append('\n');
            output.append(hoverEvent);
        }
        for (Text sibling : node.getSiblings()) {
            collectHoverText(sibling, output);
        }
    }

    public boolean interceptClear(ChatHud hud, boolean clearHistory) {
        if (Boolean.TRUE.equals(internalCall.get()) || !initialized) {
            return false;
        }
        ChatTab active = activeTab();
        active.setVanillaState(hud.toChatState());
        for (ChatTab tab : tabs()) {
            TabSettings settings = resolved(tab);
            if (settings.isProtectFromClear()) {
                if (configManager.global().isDebug()) {
                    logger.debug("Preserved chat tab {} from a server clear", tab.getName());
                }
                continue;
            }
            restore(hud, tab);
            internalCall.set(true);
            try {
                hud.clear(clearHistory);
                tab.getHistory().clear();
                tab.setVanillaState(hud.toChatState());
            } finally {
                internalCall.set(false);
            }
        }
        restore(hud, active);
        historyStore.requestSave(identity.key(), tabs(), configManager.global().getDefaultTabSettings());
        return true;
    }

    private void addToTab(ChatHud hud, ChatTab tab, ChatMessage<VanillaChatPayload> message, List<FilterAction> actions) {
        restore(hud, tab);
        TabSettings settings = resolved(tab);
        tab.getHistory().setMaximumSize(settings.getMaximumLines());
        Text decorated = decorate(message, actions, settings, 1);
        MessageIndicator visibleIndicator = settings.isShowTrustIndicators() ? message.richText().indicator() : null;
        VanillaChatPayload decoratedPayload = new VanillaChatPayload(
                decorated,
                message.richText().signature(),
                visibleIndicator,
                backgroundColor(actions),
                backgroundAlpha(actions)
        );
        ChatMessage<VanillaChatPayload> stored = new ChatMessage<>(
                message.id(), decoratedPayload, message.plainText(), message.hoverText(), message.sender(), message.type(), message.trustStatus(), message.receivedAt()
        );

        boolean merged = false;
        ChatHistory.Entry<VanillaChatPayload> entry;
        if (settings.isMergeIdenticalMessages()) {
            ChatHistory.Entry<VanillaChatPayload> previous = tab.getHistory().last();
            merged = previous != null && mergeManager.canMerge(
                    previous.message().plainText(), previous.lastSeen(), stored.plainText(), stored.receivedAt(), Duration.ofMillis(settings.getMergeIntervalMillis())
            );
            entry = mergeManager.mergeOrAppend(tab.getHistory(), stored, Duration.ofMillis(settings.getMergeIntervalMillis()));
        } else {
            tab.getHistory().add(stored);
            entry = tab.getHistory().last();
        }

        internalCall.set(true);
        try {
            if (merged) {
                hud.clear(true);
                for (ChatHistory.Entry<VanillaChatPayload> historyEntry : tab.getHistory().snapshot()) {
                    VanillaChatPayload historyPayload = historyEntry.message().richText();
                    Text rendered = historyEntry.repeatCount() > 1 && settings.isShowRepeatCounter()
                            ? Text.empty().append(historyPayload.text().copy()).append(Text.literal(" ×" + historyEntry.repeatCount()).formatted(Formatting.GRAY))
                            : historyPayload.text();
                    hud.addMessage(rendered, historyPayload.signature(), settings.isShowTrustIndicators() ? historyPayload.indicator() : null);
                }
            } else {
                Text rendered = entry.repeatCount() > 1 && settings.isShowRepeatCounter()
                        ? Text.empty().append(decorated.copy()).append(Text.literal(" ×" + entry.repeatCount()).formatted(Formatting.GRAY))
                        : decorated;
                hud.addMessage(rendered, message.richText().signature(), visibleIndicator);
            }
            tab.setVanillaState(hud.toChatState());
        } finally {
            internalCall.set(false);
        }

        if (!tab.getId().equals(activeTab().getId())) {
            tab.incrementUnread();
        }
    }

    private Text decorate(ChatMessage<VanillaChatPayload> message, List<FilterAction> actions, TabSettings settings, int count) {
        MutableText result = Text.empty();
        if (settings.isShowTimestamps()) {
            result.append(Text.literal("[" + formatTime(message.receivedAt(), settings.getTimestampFormat()) + "] ").styled(style -> style.withColor(settings.getTimestampColor() & 0xFFFFFF)));
        }
        for (FilterAction action : actions) {
            if (action.getType() == FilterActionType.ADD_ICON && action.getIcon() != null && !action.getIcon().isBlank()) {
                result.append(Text.literal(action.getIcon() + " ").formatted(Formatting.GOLD));
            }
        }
        MutableText body = message.richText().text().copy();
        for (FilterAction action : actions) {
            if (action.getType() == FilterActionType.SET_TEXT_COLOR) {
                body = body.styled(style -> style.withColor(action.getColor() & 0xFFFFFF));
            }
            if (action.getType() == FilterActionType.MARK_IMPORTANT) {
                body = Text.empty().append(Text.literal("! ").formatted(Formatting.RED, Formatting.BOLD)).append(body);
            }
        }
        result.append(body);
        if (count > 1 && settings.isShowRepeatCounter()) {
            result.append(Text.literal(" ×" + count).formatted(Formatting.GRAY));
        }
        return result;
    }


    private Integer backgroundColor(List<FilterAction> actions) {
        Integer value = null;
        for (FilterAction action : actions) {
            if (action.getType() == FilterActionType.SET_BACKGROUND_COLOR) value = action.getColor();
        }
        return value;
    }

    private Float backgroundAlpha(List<FilterAction> actions) {
        Float value = null;
        for (FilterAction action : actions) {
            if (action.getType() == FilterActionType.SET_BACKGROUND_ALPHA) value = action.getAlpha();
        }
        return value;
    }

    private String formatTime(Instant instant, String pattern) {
        try {
            return DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault()).format(instant);
        } catch (RuntimeException ignored) {
            return DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault()).format(instant);
        }
    }

    private void executeActions(List<FilterAction> actions, ChatMessage<VanillaChatPayload> message) {
        boolean soundPlayed = false;
        boolean notified = false;
        for (FilterAction action : actions) {
            if (action.getType() == FilterActionType.PLAY_SOUND && !soundPlayed && playSound(action)) {
                soundPlayed = true;
            } else if (action.getType() == FilterActionType.SHOW_NOTIFICATION && !notified) {
                internalOverlay.set(true);
                try {
                    client.inGameHud.setOverlayMessage(Text.literal(message.plainText()), false);
                } finally {
                    internalOverlay.set(false);
                }
                notified = true;
            }
        }
    }

    public boolean playSound(FilterAction action) {
        if (action == null) return false;
        Identifier identifier = Identifier.tryParse(action.getSoundId());
        if (identifier == null) {
            if (configManager.global().isDebug()) logger.debug("Invalid sound identifier: {}", action.getSoundId());
            return false;
        }
        SoundEvent sound = Registries.SOUND_EVENT.get(identifier);
        if (sound == null) {
            if (configManager.global().isDebug()) logger.debug("Unknown sound identifier: {}", identifier);
            return false;
        }
        String cooldownKey = identifier + ":" + action.getPitch() + ":" + action.getVolume();
        long now = System.currentTimeMillis();
        long last = soundCooldowns.getOrDefault(cooldownKey, Long.MIN_VALUE / 2);
        if (now - last < action.getCooldownMillis()) {
            return false;
        }
        client.getSoundManager().play(PositionedSoundInstance.ui(sound, action.getPitch(), action.getVolume()));
        soundCooldowns.put(cooldownKey, now);
        return true;
    }

    public void switchTo(UUID tabId) {
        if (!initialized || client.inGameHud == null) return;
        find(tabId).ifPresent(target -> {
            ChatHud hud = client.inGameHud.getChatHud();
            activeTab().setVanillaState(hud.toChatState());
            serverConfig.setActiveTabId(target.getId());
            restore(hud, target);
            target.setUnreadCount(0);
            configManager.requestSave();
        });
    }

    public void nextTab(int direction) {
        List<ChatTab> ordered = tabs();
        if (ordered.isEmpty()) return;
        int current = Math.max(0, ordered.indexOf(activeTab()));
        switchTo(ordered.get(Math.floorMod(current + direction, ordered.size())).getId());
    }

    public ChatTab createTab(String name) {
        ChatTab tab = new ChatTab(name, false, tabs().size());
        serverConfig.getTabs().add(tab);
        createEmptyState(tab);
        switchTo(tab.getId());
        configManager.requestSave();
        historyStore.requestSave(identity.key(), tabs(), configManager.global().getDefaultTabSettings());
        return tab;
    }

    public ChatTab duplicate(ChatTab source) {
        ChatTab tab = source.duplicate();
        serverConfig.getTabs().add(tab);
        createEmptyState(tab);
        normalizeOrder();
        configManager.requestSave();
        historyStore.requestSave(identity.key(), tabs(), configManager.global().getDefaultTabSettings());
        return tab;
    }

    public boolean delete(UUID id) {
        Optional<ChatTab> tab = find(id);
        if (tab.isEmpty() || tab.get().isSystemTab()) return false;
        boolean active = activeTab().getId().equals(id);
        serverConfig.getTabs().remove(tab.get());
        normalizeOrder();
        if (active) switchTo(tabs().getFirst().getId());
        configManager.requestSave();
        historyStore.requestSave(identity.key(), tabs(), configManager.global().getDefaultTabSettings());
        return true;
    }

    public void clearTab(ChatTab tab) {
        if (client.inGameHud == null || tab == null) return;
        ChatHud hud = client.inGameHud.getChatHud();
        ChatTab active = activeTab();
        active.setVanillaState(hud.toChatState());
        restore(hud, tab);
        internalCall.set(true);
        try {
            hud.clear(true);
            tab.getHistory().clear();
            tab.setVanillaState(hud.toChatState());
        } finally {
            internalCall.set(false);
        }
        restore(hud, active);
        historyStore.requestSave(identity.key(), tabs(), configManager.global().getDefaultTabSettings());
    }

    public void move(ChatTab tab, int offset) {
        List<ChatTab> values = serverConfig.getTabs();
        int from = values.indexOf(tab);
        int to = Math.max(0, Math.min(values.size() - 1, from + offset));
        if (from >= 0 && from != to) {
            values.remove(from);
            values.add(to, tab);
            normalizeOrder();
            configManager.requestSave();
        }
    }

    private void initializeStates(ChatHud hud) {
        if (initialized) return;
        ChatHud.ChatState original = hud.toChatState();
        ChatTab active = activeTab();
        for (ChatTab tab : tabs()) {
            if (tab.getHistory().size() > 0) {
                tab.setVanillaState(stateFromHistory(hud, tab, original));
            } else if (tab == active) {
                tab.setVanillaState(original);
            } else {
                tab.setVanillaState(emptyState(hud, original));
            }
        }
        restore(hud, active);
        initialized = true;
    }

    private void createEmptyState(ChatTab tab) {
        if (client.inGameHud == null) return;
        ChatHud hud = client.inGameHud.getChatHud();
        ChatHud.ChatState original = hud.toChatState();
        tab.setVanillaState(emptyState(hud, original));
        hud.restoreChatState(original);
    }

    private ChatHud.ChatState emptyState(ChatHud hud, ChatHud.ChatState original) {
        internalCall.set(true);
        try {
            hud.clear(true);
            return hud.toChatState();
        } finally {
            hud.restoreChatState(original);
            internalCall.set(false);
        }
    }

    private ChatHud.ChatState stateFromHistory(ChatHud hud, ChatTab tab, ChatHud.ChatState original) {
        internalCall.set(true);
        try {
            hud.clear(true);
            TabSettings settings = resolved(tab);
            for (ChatHistory.Entry<VanillaChatPayload> entry : tab.getHistory().snapshot()) {
                VanillaChatPayload payload = entry.message().richText();
                Text rendered = entry.repeatCount() > 1 && settings.isShowRepeatCounter()
                        ? Text.empty().append(payload.text().copy()).append(Text.literal(" ×" + entry.repeatCount()).formatted(Formatting.GRAY))
                        : payload.text();
                hud.addMessage(rendered, payload.signature(), settings.isShowTrustIndicators() ? payload.indicator() : null);
            }
            return hud.toChatState();
        } finally {
            hud.restoreChatState(original);
            internalCall.set(false);
        }
    }

    private void restore(ChatHud hud, ChatTab tab) {
        if (tab.getVanillaState() != null) {
            hud.restoreChatState(tab.getVanillaState());
        }
    }

    private void switchProfile(ServerIdentity next) {
        if (client.inGameHud != null && initialized) {
            activeTab().setVanillaState(client.inGameHud.getChatHud().toChatState());
        }
        for (ChatTab tab : tabs()) {
            if (resolved(tab).isClearOnServerChange()) {
                tab.getHistory().clear();
                tab.setVanillaState(null);
            }
        }
        historyStore.saveNow(identity.key(), tabs(), configManager.global().getDefaultTabSettings());
        identity = next;
        serverConfig = configManager.server(next.key(), next.displayName());
        historyStore.load(next.key(), serverConfig.getTabs(), configManager.global().getDefaultTabSettings());
        initialized = false;
        if (client.inGameHud != null) initializeStates(client.inGameHud.getChatHud());
        configManager.requestSave();
    }


    public void reloadProfile() {
        ServerIdentity current = ServerIdentity.current(client);
        identity = current;
        serverConfig = configManager.server(current.key(), current.displayName());
        historyStore.load(current.key(), serverConfig.getTabs(), configManager.global().getDefaultTabSettings());
        initialized = false;
        if (client.inGameHud != null) {
            initializeStates(client.inGameHud.getChatHud());
        }
    }

    private void normalizeOrder() {
        for (int index = 0; index < serverConfig.getTabs().size(); index++) {
            serverConfig.getTabs().get(index).setOrder(index);
        }
    }

    public List<ChatTab> tabs() {
        List<ChatTab> result = new ArrayList<>(serverConfig.getTabs());
        result.sort(Comparator.comparing(ChatTab::isPinned).reversed().thenComparingInt(ChatTab::getOrder));
        return result;
    }

    public ChatTab activeTab() {
        return find(serverConfig.getActiveTabId()).orElseGet(() -> tabs().getFirst());
    }

    public Optional<ChatTab> find(UUID id) {
        if (id == null) return Optional.empty();
        return serverConfig.getTabs().stream().filter(tab -> id.equals(tab.getId())).findFirst();
    }

    public TabSettings resolved(ChatTab tab) {
        return tab.resolvedSettings(configManager.global().getDefaultTabSettings());
    }


    public void shutdown() {
        historyStore.saveNow(identity.key(), tabs(), configManager.global().getDefaultTabSettings());
        historyStore.close();
    }

    public ServerConfig serverConfig() { return serverConfig; }
    public ServerIdentity identity() { return identity; }
}
