package cn.islys.hud;

import cn.islys.config.AutoMessengerConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class CountdownHud {
    private int remainingMs = 0;
    private static final int COLOR_NORMAL = 0xFFFFFF;
    private static final int COLOR_WARNING = 0xFFAA00;
    private static final int COLOR_URGENT = 0xFF5555;

    public void register() {
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            this.render(guiGraphics);
        });
    }

    public void updateCountdown(int ms) {
        this.remainingMs = ms;
    }

    private void render(GuiGraphics guiGraphics) {
        AutoMessengerConfig config = AutoMessengerConfig.getInstance();

        // 检查是否应该显示
        if (!config.showCountdown || !config.enableScheduledMessages || !config.enableLoop) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        // 没有内容时不显示
        if (remainingMs <= 0 && !config.enableLoop) return;

        Font font = client.font;
        int screenHeight = client.getWindow().getGuiScaledHeight();

        String timeText = formatTime(remainingMs);
        String label = config.scheduledMessageList.isEmpty() ? "" : "下次发送: ";
        String display = label + timeText;

        int color;
        if (remainingMs <= 5000) color = COLOR_URGENT;      // 5秒内红色
        else if (remainingMs <= 10000) color = COLOR_WARNING; // 10秒内黄色
        else color = COLOR_NORMAL;

        int x = 10;
        int y = screenHeight - 30;

        int textWidth = font.width(display);
        guiGraphics.fill(x - 2, y - 2, x + textWidth + 2, y + font.lineHeight + 2, 0x80000000);
        guiGraphics.drawString(font, display, x, y, color, true);
    }

    private String formatTime(int ms) {
        if (ms < 0) ms = 0;

        int seconds = ms / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}