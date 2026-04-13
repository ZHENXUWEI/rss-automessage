package cn.islys.hud;

import cn.islys.config.AutoMessengerConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class CountdownHud {
    private int remainingMs = 0;
    private int lastLoggedSecond = -1;
    private static final int COLOR_NORMAL = 0xFFFFFF;   // 纯白色，最醒目
    private static final int COLOR_WARNING = 0xFFAA00;  // 橙色
    private static final int COLOR_URGENT = 0xFF0000;   // 纯红色

    public void register() {
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            render(guiGraphics);
        });
    }

    public void updateCountdown(int ms) {
        this.remainingMs = ms;
    }

    private void render(GuiGraphics guiGraphics) {
        AutoMessengerConfig config = AutoMessengerConfig.getInstance();

        if (!config.showCountdown) return;
        if (!config.enableScheduledMessages) return;
        if (config.scheduledMessageList == null || config.scheduledMessageList.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        Font font = client.font;
        if (font == null) return;  // 安全检查

        int screenHeight = client.getWindow().getGuiScaledHeight();
        int screenWidth = client.getWindow().getGuiScaledWidth();

        // 硬编码ASCII字符，避免编码问题
        String timeStr = formatTime(remainingMs);
        String text = "Next: " + timeStr;

        int color;
        if (remainingMs <= 5000) color = COLOR_URGENT;
        else if (remainingMs <= 10000) color = COLOR_WARNING;
        else color = COLOR_NORMAL;

        // 紧贴左下角
        int x = 2;
        int y = screenHeight - 11;  // 微调，对齐像素

        int textWidth = font.width(text);
        int textHeight = 8;  // 字体高度

        // 绘制背景（先画背景，再画文字）
        guiGraphics.fill(x - 2, y - 2, x + textWidth + 4, y + textHeight + 2, 0x44000000);

        // 绘制文字（强制白色，带阴影）
        guiGraphics.drawString(font, text, x, y, 0xFFFFFFFF, true);

        // 每秒日志一次
        int currentSecond = remainingMs / 1000;
        if (currentSecond != lastLoggedSecond) {
            System.out.println("[AutoMessage] Countdown: " + text + " (Color: " + Integer.toHexString(color) + ")");
            lastLoggedSecond = currentSecond;
        }
    }

    private String formatTime(int ms) {
        if (ms < 0) ms = 0;
        int totalSeconds = ms / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        // 纯ASCII格式
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }
}