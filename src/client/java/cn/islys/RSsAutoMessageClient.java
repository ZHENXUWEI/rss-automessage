package cn.islys;

import cn.islys.config.AutoMessengerConfig;
import cn.islys.config.AutoMessengerConfig.MessageEntry;
import cn.islys.hud.CountdownHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

import java.util.List;

public class RSsAutoMessageClient implements ClientModInitializer {

    private static RSsAutoMessageClient instance;
    private final MessageScheduler scheduler = new MessageScheduler();
    private final AutoReplyHandler autoReply = new AutoReplyHandler();
    private final CountdownHud countdownHud = new CountdownHud();

    // 定时器（毫秒计算）
    private long lastSendTime = 0;
    private int remainingMs = 0;

    @Override
    public void onInitializeClient() {
        instance = this;

        // 加载配置
        AutoMessengerConfig.load();

        // 注册进入服务器事件
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            onServerJoined();
        });

        // 注册离开服务器事件
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            onServerLeft();
        });

        // 注册接收聊天消息事件（用于自动回复）
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                autoReply.onChatMessage(message.getString());
            }
        });

        // 注册客户端 Tick 事件
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            onClientTick();
        });

        // 注册 HUD 渲染
        countdownHud.register();
    }

    private void onServerJoined() {
        AutoMessengerConfig config = AutoMessengerConfig.getInstance();

        // 发送加入时的消息（一次性发送所有）
        if (config.sendOnJoin && config.enableScheduledMessages) {
            sendAllScheduledMessages();
        }

        // 初始化倒计时
        if (config.enableScheduledMessages) {
            remainingMs = config.sendIntervalMs;
            countdownHud.updateCountdown(remainingMs);
        }

        lastSendTime = System.currentTimeMillis();
        AutoMessengerConfig.save();
    }

    private void onServerLeft() {
        remainingMs = 0;
        countdownHud.updateCountdown(0);
    }

    private void onClientTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.getConnection() == null) return;

        AutoMessengerConfig config = AutoMessengerConfig.getInstance();
        if (!config.enableScheduledMessages || !config.enableLoop) return;

        // 计算剩余时间
        long now = System.currentTimeMillis();
        int elapsed = (int) (now - lastSendTime);
        remainingMs = Math.max(0, config.sendIntervalMs - elapsed);

        // 更新倒计时显示
        countdownHud.updateCountdown(remainingMs);

        // 时间到了，发送所有消息
        if (remainingMs <= 0) {
            sendAllScheduledMessages();
            lastSendTime = now;
            remainingMs = config.sendIntervalMs;
            AutoMessengerConfig.save();
        }
    }

    // 一次性发送列表中的所有消息
    private void sendAllScheduledMessages() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        AutoMessengerConfig config = AutoMessengerConfig.getInstance();
        List<MessageEntry> messages = config.getScheduledMessages();

        for (MessageEntry entry : messages) {
            String msg = entry.getSendText();
            if (msg.isEmpty()) continue;

            if (entry.isCommand) {
                client.player.connection.sendCommand(msg);
            } else {
                client.player.connection.sendChat(msg);
            }

            // 每条消息间隔100ms，避免发送过快
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void sendMessage(String message) {
        scheduler.sendMessage(message);
    }

    public void resetCountdown() {
        AutoMessengerConfig config = AutoMessengerConfig.getInstance();
        lastSendTime = System.currentTimeMillis();
        remainingMs = config.sendIntervalMs;
        countdownHud.updateCountdown(remainingMs);
    }

    public int getRemainingMs() { return remainingMs; }
    public MessageScheduler getScheduler() { return scheduler; }
    public AutoReplyHandler getAutoReply() { return autoReply; }
    public CountdownHud getCountdownHud() { return countdownHud; }
    public static RSsAutoMessageClient getInstance() { return instance; }
}