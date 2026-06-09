package com.example.advancedchattabs.filter;

import java.util.List;

public record FilterTestResult(
        boolean matched,
        String matchedCondition,
        String excludedBy,
        String error,
        List<FilterAction> actions
) {
    public static FilterTestResult noMatch(String reason) {
        return new FilterTestResult(false, "", reason, "", List.of());
    }

    public static FilterTestResult error(String error) {
        return new FilterTestResult(false, "", "", error, List.of());
    }
}
