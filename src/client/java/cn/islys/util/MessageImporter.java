package cn.islys.util;

import cn.islys.config.AutoMessengerConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MessageImporter {

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir()
            .resolve("rss-automessage");
    private static final Path DEFAULT_FILE = CONFIG_DIR.resolve("messages.txt");

    /**
     * 智能解析行
     * 格式规则：
     * - # 开头 = 注释（忽略）
     * - / 开头 = 指令（去掉开头的 /）
     * - 其他 = 普通消息
     */
    public static String parseLine(String line) {
        String trimmed = line.trim();

        // 空行
        if (trimmed.isEmpty()) {
            return null;
        }

        // 注释行（# 或 //）
        if (trimmed.startsWith("#") || trimmed.startsWith("//")) {
            return null;
        }

        // 指令行（/ 开头）
        if (trimmed.startsWith("/")) {
            // 去掉开头的 /，并标记为指令
            String cmd = trimmed.substring(1).trim();
            if (cmd.isEmpty()) return null;
            return cmd + "|cmd";
        }

        // 普通消息
        return trimmed + "|msg";
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
        if (!Files.exists(file)) {
            return new ImportResult(false, 0, "文件不存在: " + file.getFileName(), null);
        }

        List<String> messages = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int lineNumber = 0;
        int commentCount = 0;
        int commandCount = 0;
        int messageCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;

                String parsed = parseLine(line);
                if (parsed == null) {
                    if (line.trim().startsWith("#") || line.trim().startsWith("//")) {
                        commentCount++;
                    }
                    continue; // 跳过注释和空行
                }

                // 统计
                if (parsed.endsWith("|cmd")) commandCount++;
                else messageCount++;

                messages.add(parsed);

                // 每1000行让出线程
                if (lineNumber % 1000 == 0) {
                    Thread.yield();
                }
            }
        } catch (IOException e) {
            return new ImportResult(false, 0, "读取失败: " + e.getMessage(), null);
        }

        if (messages.isEmpty()) {
            return new ImportResult(false, 0, "没有找到有效的消息（全是注释或空行）", null);
        }

        // 更新配置
        AutoMessengerConfig config = AutoMessengerConfig.getInstance();
        config.scheduledMessageList.clear();
        config.scheduledMessageList.addAll(messages);
        AutoMessengerConfig.save();

        StringBuilder msg = new StringBuilder();
        msg.append("导入 ").append(messages.size()).append(" 条");
        if (commandCount > 0) msg.append(" (").append(commandCount).append(" 条指令)");
        if (messageCount > 0) msg.append(" (").append(messageCount).append(" 条消息)");
        if (commentCount > 0) msg.append("，跳过 ").append(commentCount).append(" 行注释");

        return new ImportResult(true, messages.size(), msg.toString(), errors);
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