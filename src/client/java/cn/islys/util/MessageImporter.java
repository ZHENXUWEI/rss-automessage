package cn.islys.util;

import cn.islys.config.AutoMessengerConfig;
import cn.islys.RSsAutoMessageClient;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MessageImporter {

    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get()
            .resolve("rss-automessage");
    private static final Path DEFAULT_FILE = CONFIG_DIR.resolve("messages.txt");
    private static final TextFileLibrary TEXT_LIBRARY = new TextFileLibrary(FMLPaths.GAMEDIR.get());

    public static TextFileLibrary getTextLibrary() {
        return TEXT_LIBRARY;
    }

    /** Apply a fully read snapshot on the Minecraft client thread. */
    public static void applyTextFile(String filename, TextFileLibrary.Content content) {
        AutoMessengerConfig config = AutoMessengerConfig.getInstance();
        config.scheduledMessageList = new ArrayList<>(content.messages());
        config.selectedTextFile = filename;
        config.currentMessageIndex = 0;
        RSsAutoMessageClient client = RSsAutoMessageClient.getInstance();
        if (client != null) client.onMessageListChanged();
        AutoMessengerConfig.save();
    }

    /**
     * 智能解析行
     * 格式规则：
     * - # 开头 = 注释（忽略）
     * - / 开头 = 指令（去掉开头的 /）
     * - 其他 = 普通消息
     */
    public static String parseLine(String line) {
        return TextFileLibrary.parseLine(line);
    }

    /**
     * 从默认路径导入
     */
    public static ImportResult importFromDefault() {
        return importFromFile(DEFAULT_FILE);
    }

    /**
     * 从指定路径导入（智能格式）
     */
    public static ImportResult importFromFile(Path file) {
        TextFileLibrary.Content content;
        try {
            content = TextFileLibrary.readFile(file);
        } catch (IOException e) {
            return new ImportResult(false, 0, "读取失败: " + e.getMessage(), null);
        }
        Minecraft client = Minecraft.getInstance();
        if (client.isSameThread()) {
            applyTextFile("", content);
        } else {
            client.submit(() -> applyTextFile("", content)).join();
        }

        int commandCount = content.commandCount();
        int commentCount = content.commentCount();
        int messageCount = content.messages().size() - commandCount;

        StringBuilder msg = new StringBuilder();
        msg.append("导入 ").append(content.messages().size()).append(" 条");
        if (commandCount > 0) msg.append(" (").append(commandCount).append(" 条指令)");
        if (messageCount > 0) msg.append(" (").append(messageCount).append(" 条消息)");
        if (commentCount > 0) msg.append("，跳过 ").append(commentCount).append(" 行注释");

        return new ImportResult(true, content.messages().size(), msg.toString(), List.of());
    }

    /**
     * 导出当前列表到TXT（使用新格式）
     */
    public static boolean exportToDefault() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Path file = DEFAULT_FILE;

            List<String> lines = new ArrayList<>();
            lines.add("# RS's AutoMessage 消息列表");
            lines.add("# 格式说明：");
            lines.add("#   # 开头 = 注释（会被忽略）");
            lines.add("#   / 开头 = 指令（自动执行，不需要|cmd后缀）");
            lines.add("#   其他   = 普通消息（自动添加|msg后缀）");
            lines.add("#");
            lines.add("# 示例：");
            lines.add("# 这是注释");
            lines.add("/warp spawn      # 这是指令");
            lines.add("大家好！         # 这是普通消息");
            lines.add("");

            AutoMessengerConfig config = AutoMessengerConfig.getInstance();
            for (String entry : config.scheduledMessageList) {
                // 反向解析为易读格式
                String[] parts = entry.split("\\|", 2);
                String content = parts[0];
                String type = parts.length > 1 ? parts[1] : "msg";

                if ("cmd".equals(type)) {
                    lines.add("/" + content);
                } else {
                    lines.add(content);
                }
            }

            Files.write(file, lines, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建示例文件（使用新格式）
     */
    public static void createExampleFile() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Path file = CONFIG_DIR.resolve("messages-example.txt");

            List<String> example = new ArrayList<>();
            example.add("# ========== RS's AutoMessage 示例文件 ==========");
            example.add("# 每行一条消息，支持以下格式：");
            example.add("#");
            example.add("# 1. 注释行（以 # 或 // 开头）");
            example.add("// 这也是注释");
            example.add("#");
            example.add("# 2. 指令行（以 / 开头，自动识别为指令）");
            example.add("/warp spawn");
            example.add("/gamemode survival");
            example.add("/sethome home");
            example.add("#");
            example.add("# 3. 普通消息（无特殊前缀）");
            example.add("欢迎来到服务器！");
            example.add("请遵守游戏规则，文明游戏");
            example.add("有问题请联系管理员");
            example.add("");
            example.add("# 实际导入时会自动转换格式：");
            example.add("# /warp spawn     →  warp spawn|cmd");
            example.add("# 欢迎！          →  欢迎！|msg");

            Files.write(file, example, StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    public record ImportResult(boolean success, int count, String message, List<String> errors) {}

    public static Path getConfigDir() {
        return CONFIG_DIR;
    }
}
