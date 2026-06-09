package com.example.advancedchattabs.util;

public final class ColorUtil {
    private ColorUtil() {}

    public static int withAlpha(int rgb, float alpha) {
        int a = Math.round(Math.max(0.0F, Math.min(1.0F, alpha)) * 255.0F);
        return (a << 24) | (rgb & 0x00FFFFFF);
    }
}
