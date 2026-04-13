package cn.islys;

import cn.islys.config.AutoMessengerConfig;
import net.minecraft.client.Minecraft;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoReplyHandler {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private long lastReplyTime = 0;

    /**
     * 处理接收到的聊天消息
     */
    public void onChatMessage(String message) {
        AutoMessengerConfig config = AutoMessengerConfig.getInstance();

        if (!config.enableAutoReply) return;
        if (message == null || message.trim().isEmpty()) return;

        // 检查冷却时间
        long now = System.currentTimeMillis();
        if (now - lastReplyTime < config.autoReplyCooldownMs) return;

        // 检查是否匹配任一关键词
        String lowerMsg = message.toLowerCase();
        boolean shouldReply = false;

        for (String keyword : config.autoReplyTriggers) {
            if (keyword != null && !keyword.isEmpty()
                    && lowerMsg.contains(keyword.toLowerCase())) {
                shouldReply = true;
                break;
            }
        }

        if (shouldReply) {
            lastReplyTime = now;
            // 随机延迟 500-1500ms
            int delay = 500 + (int) (Math.random() * 1000);
            executor.schedule(this::sendAutoReply, delay, TimeUnit.MILLISECONDS);
        }
    }

    private void sendAutoReply() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        String reply = AutoMessengerConfig.getInstance().autoReplyMessage;
        if (reply == null || reply.trim().isEmpty()) return;

        if (reply.startsWith("/")) {
            client.player.connection.sendCommand(reply.substring(1));
        } else {
            client.player.connection.sendChat(reply);
        }
    }

    public void resetCooldown() {
        lastReplyTime = 0;
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}