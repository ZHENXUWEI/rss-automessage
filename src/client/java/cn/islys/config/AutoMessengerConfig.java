package cn.islys.config;

import cn.islys.RSsAutoMessageClient;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoMessengerConfig {
    public static final ConfigClassHandler<AutoMessengerConfig> HANDLER =
            ConfigClassHandler.createBuilder(AutoMessengerConfig.class)
                    .id(ResourceLocation.fromNamespaceAndPath("rss-automessage", "config"))
                    .serializer(config -> GsonConfigSerializerBuilder.create(config)
                            .setPath(FabricLoader.getInstance().getConfigDir().resolve("rss-automessage.json"))
                            .build())
                    .build();

    @SerialEntry
    public boolean enableScheduledMessages = true;

    @SerialEntry
    public boolean sendOnJoin = true;

    @SerialEntry
    public List<String> scheduledMessageList = new ArrayList<>(Arrays.asList(
            "欢迎来到服务器！|msg",
            "warp spawn|cmd",
            "祝游戏愉快！|msg"
    ));

    @SerialEntry
    public int currentMessageIndex = 0;

    @SerialEntry
    public boolean enableLoop = true;

    @SerialEntry
    public int sendIntervalMs = 300000; // 默认5分钟 = 300000ms

    @SerialEntry
    public boolean enableAutoReply = false;

    @SerialEntry
    public List<String> autoReplyTriggers = new ArrayList<>(Arrays.asList(
            "你好",
            "hello",
            "在吗",
            "有人吗"
    ));

    @SerialEntry
    public String autoReplyMessage = "我在挂机，稍后回复~";

    @SerialEntry
    public int autoReplyCooldownMs = 3000;

    @SerialEntry
    public boolean showCountdown = true;

    // ===== 新增字段 =====
    @SerialEntry
    public boolean showAllMessages = false;  // 是否显示全部消息（警告：大量数据可能卡顿）

    @SerialEntry
    public boolean randomSend = false;  // 随机发送顺序

    public static AutoMessengerConfig getInstance() {
        return HANDLER.instance();
    }

    public static void load() {
        HANDLER.load();
    }

    public static void save() {
        HANDLER.save();
        // 如果禁用功能，通知客户端停止发送
        if (!getInstance().enableScheduledMessages) {
            RSsAutoMessageClient client = RSsAutoMessageClient.getInstance();
            if (client != null) {
                client.stopAllSending();
            }
        }
    }

    // 解析消息条目
    public static class MessageEntry {
        public final String content;
        public final boolean isCommand;

        public MessageEntry(String raw) {
            String[] parts = raw.split("\\|", 2);
            this.content = parts[0];
            this.isCommand = parts.length > 1 && "cmd".equalsIgnoreCase(parts[1]);
        }

        public String getSendText() {
            if (isCommand && content.startsWith("/")) {
                return content.substring(1);
            }
            return content;
        }
    }

    // 获取所有定时消息
    public List<MessageEntry> getScheduledMessages() {
        List<MessageEntry> list = new ArrayList<>();
        for (String raw : scheduledMessageList) {
            if (!raw.trim().isEmpty()) {
                list.add(new MessageEntry(raw));
            }
        }
        return list;
    }
}