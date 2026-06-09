package com.example.advancedchattabs.filter;

import com.example.advancedchattabs.chat.ChatMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class FilterEngine {
    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();

    public FilterTestResult test(ChatFilter filter, ChatMessage<?> message) {
        if (filter == null || !filter.isEnabled()) {
            return FilterTestResult.noMatch("disabled");
        }
        if (!filter.getMessageTypes().contains(message.type())) {
            return FilterTestResult.noMatch("message type");
        }
        if (!filter.getTrustStatuses().contains(message.trustStatus())) {
            return FilterTestResult.noMatch("trust status");
        }

        String value = message.searchableText(filter.isInspectHover(), filter.isInspectSender());
        if (!filter.isPlainTextOnly() && message.richText() != null) {
            value = value + "\n" + message.richText();
        }
        MatchMode mode = filter.isUseRegularExpression() ? MatchMode.REGEX : filter.getMatchMode();

        for (String excluded : normalizedConditions(filter.getExcludeConditions())) {
            MatchOutcome outcome = matches(value, excluded, mode, filter.isCaseSensitive());
            if (outcome.error() != null) {
                return FilterTestResult.error(outcome.error());
            }
            if (outcome.matched()) {
                return new FilterTestResult(false, "", excluded, "", List.of());
            }
        }

        List<String> includes = normalizedConditions(filter.getIncludeConditions());
        if (includes.isEmpty()) {
            return new FilterTestResult(true, "<all>", "", "", List.copyOf(filter.getActions()));
        }

        String firstMatch = "";
        boolean matched = filter.getLogicalMode() == LogicalMode.ALL;
        for (String include : includes) {
            MatchOutcome outcome = matches(value, include, mode, filter.isCaseSensitive());
            if (outcome.error() != null) {
                return FilterTestResult.error(outcome.error());
            }
            if (outcome.matched() && firstMatch.isEmpty()) {
                firstMatch = include;
            }
            if (filter.getLogicalMode() == LogicalMode.ANY && outcome.matched()) {
                matched = true;
                break;
            }
            if (filter.getLogicalMode() == LogicalMode.ALL && !outcome.matched()) {
                matched = false;
                break;
            }
        }
        return new FilterTestResult(matched, firstMatch, "", "", matched ? List.copyOf(filter.getActions()) : List.of());
    }

    public List<FilterTestResult> evaluate(List<ChatFilter> filters, ChatMessage<?> message) {
        List<ChatFilter> sorted = new ArrayList<>(filters == null ? List.of() : filters);
        sorted.sort(Comparator.comparingInt(ChatFilter::getPriority).reversed());
        List<FilterTestResult> results = new ArrayList<>();
        for (ChatFilter filter : sorted) {
            FilterTestResult result = test(filter, message);
            results.add(result);
            if (result.matched() && filter.isStopAfterMatch()) {
                break;
            }
        }
        return results;
    }

    public void invalidatePatterns() {
        patternCache.clear();
    }

    private MatchOutcome matches(String value, String condition, MatchMode mode, boolean caseSensitive) {
        String left = caseSensitive ? value : value.toLowerCase(Locale.ROOT);
        String right = caseSensitive ? condition : condition.toLowerCase(Locale.ROOT);
        return switch (mode) {
            case CONTAINS -> new MatchOutcome(left.contains(right), null);
            case STARTS_WITH -> new MatchOutcome(left.startsWith(right), null);
            case ENDS_WITH -> new MatchOutcome(left.endsWith(right), null);
            case EXACT -> new MatchOutcome(left.equals(right), null);
            case REGEX -> regex(value, condition, caseSensitive);
        };
    }

    private MatchOutcome regex(String value, String expression, boolean caseSensitive) {
        String key = (caseSensitive ? "1:" : "0:") + expression;
        try {
            Pattern pattern = patternCache.computeIfAbsent(key, ignored -> Pattern.compile(
                    expression,
                    caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
            ));
            return new MatchOutcome(pattern.matcher(value).find(), null);
        } catch (PatternSyntaxException exception) {
            return new MatchOutcome(false, exception.getDescription());
        }
    }

    private List<String> normalizedConditions(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream().map(String::trim).filter(value -> !value.isEmpty()).toList();
    }

    private record MatchOutcome(boolean matched, String error) {}
}
