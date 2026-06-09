package com.example.advancedchattabs.render;

import com.example.advancedchattabs.screen.TabContextMenuScreen;
import com.example.advancedchattabs.tab.ChatTab;
import com.example.advancedchattabs.tab.ChatTabManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class TabBarRenderer {
    private record HitBox(int x, int y, int width, int height, ChatTab tab) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }

    private final MinecraftClient client;
    private final ChatTabManager tabManager;
    private final List<HitBox> hitBoxes = new ArrayList<>();

    public TabBarRenderer(MinecraftClient client, ChatTabManager tabManager) {
        this.client = client;
        this.tabManager = tabManager;
    }

    public void render(DrawContext context, boolean interactive) {
        if (client.options.hudHidden) return;
        hitBoxes.clear();
        int x = 2;
        int y = Math.max(2, client.getWindow().getScaledHeight() - 174);
        for (ChatTab tab : tabManager.tabs()) {
            String label = tab.getName() + (tab.getUnreadCount() > 0 ? " (" + tab.getUnreadCount() + ")" : "");
            int width = Math.max(46, client.textRenderer.getWidth(label) + 12);
            int color = tab.getId().equals(tabManager.activeTab().getId()) ? 0xD0406040 : 0xB0202020;
            context.fill(x, y, x + width, y + 14, color);
            context.drawTextWithShadow(client.textRenderer, Text.literal(label), x + 6, y + 3, 0xFFFFFFFF);
            if (interactive) hitBoxes.add(new HitBox(x, y, width, 14, tab));
            x += width + 2;
            if (x > client.getWindow().getScaledWidth() - 50) break;
        }
    }

    public boolean handleClick(double mouseX, double mouseY, int button) {
        for (HitBox box : hitBoxes) {
            if (!box.contains(mouseX, mouseY)) continue;
            if (button == 0) {
                tabManager.switchTo(box.tab().getId());
            } else if (button == 1) {
                client.setScreen(new TabContextMenuScreen(client.currentScreen, box.tab()));
            }
            return true;
        }
        return false;
    }

    public boolean handleScroll(double mouseX, double mouseY, double verticalAmount) {
        if (verticalAmount == 0.0) return false;
        for (HitBox box : hitBoxes) {
            if (box.contains(mouseX, mouseY)) {
                tabManager.nextTab(verticalAmount > 0 ? -1 : 1);
                return true;
            }
        }
        return false;
    }
}
