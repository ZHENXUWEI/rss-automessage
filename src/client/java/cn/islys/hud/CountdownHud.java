package cn.islys.hud;

import cn.islys.config.AutoMessengerConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class CountdownHud {
    private int remainingMs = 0;
    private int lastLoggedSecond = -1;
    private static final int COLOR_NORMAL = 0xFFFFFFFF;
    private static final int COLOR_WARNING = 0xFFFFAA00;
    private static final int COLOR_URGENT = 0xFFFF0000;

    // 原版聊天 HUD 元素的 ID
    private static final Identifier CHAT_HUD_ID = Identifier.withDefaultNamespace("chat");
    private static final Identifier COUNTDOWN_HUD_ID = Identifier.fromNamespaceAndPath("rss-automessage", "countdown");

    public void register() {
        // ✅ 26.1 正确的 HUD API
        HudElementRegistry.attachElementBefore(
                CHAT_HUD_ID,
                COUNTDOWN_HUD_ID,
                (GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) -> {
                    render(graphics);
                }
        );
    }

    public void updateCountdown(int ms) {
        this.remainingMs = ms;
    }

    private void render(GuiGraphicsExtractor graphics) {
        AutoMessengerConfig config = AutoMessengerConfig.getInstance();

        if (!config.showCountdown) return;
        if (!config.enableScheduledMessages) return;
        if (config.scheduledMessageList == null || config.scheduledMessageList.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        Font font = client.font;
        if (font == null) return;

        int screenHeight = client.getWindow().getGuiScaledHeight();

        String timeStr = formatTime(remainingMs);
        String text = "Next: " + timeStr;

        int color;
        if (remainingMs <= 5000) color = COLOR_URGENT;
        else if (remainingMs <= 10000) color = COLOR_WARNING;
        else color = COLOR_NORMAL;

        int x = 2;
        int y = screenHeight - 11;

        int textWidth = font.width(text);
        int textHeight = font.lineHeight;

        // ✅ fill(x0, y0, x1, y1, color)
        graphics.fill(x - 2, y - 2, x + textWidth + 4, y + textHeight + 2, 0x44000000);

        // ✅ text(font, text, x, y, color, shadow)
        graphics.text(font, text, x, y, color, true);

        int currentSecond = remainingMs / 1000;
        if (currentSecond != lastLoggedSecond) {
            System.out.println("[AutoMessage] Countdown: " + text);
            lastLoggedSecond = currentSecond;
        }
    }

    private String formatTime(int ms) {
        if (ms < 0) ms = 0;
        int totalSeconds = ms / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}