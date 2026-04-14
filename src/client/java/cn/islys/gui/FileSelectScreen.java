package cn.islys.gui;

import cn.islys.util.MessageImporter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileSelectScreen extends Screen {
    private final Screen parentScreen;
    private final Path targetDir;

    private List<FileEntry> fileEntries = new ArrayList<>();
    private int selectedIndex = -1;
    private int scrollOffset = 0;

    // 布局常量
    private static final int ENTRY_HEIGHT = 25;
    private static final int LIST_TOP = 45;
    private static final int LIST_BOTTOM_MARGIN = 60; // 按钮区域预留

    public FileSelectScreen(Screen parent, Path directory) {
        super(Component.literal("选择TXT文件"));
        this.parentScreen = parent;
        this.targetDir = directory;
    }

    @Override
    protected void init() {
        refreshFileList();

        int btnY = this.height - 30;
        int btnWidth = (this.width - 50) / 4;

        // 底部按钮
        this.addRenderableWidget(Button.builder(
                Component.literal("导入选中"),
                btn -> importSelected()
        ).bounds(10, btnY, btnWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("刷新"),
                btn -> refreshFileList()
        ).bounds(20 + btnWidth, btnY, btnWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("打开文件夹"),
                btn -> openFolder()
        ).bounds(30 + btnWidth * 2, btnY, btnWidth, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.literal("返回"),
                btn -> this.minecraft.setScreen(parentScreen)
        ).bounds(40 + btnWidth * 3, btnY, btnWidth, 20).build());
    }

    private void refreshFileList() {
        fileEntries.clear();
        selectedIndex = -1;
        scrollOffset = 0;

        try {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            List<Path> files = Files.list(targetDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".txt"))
                    .sorted()
                    .collect(Collectors.toList());

            for (Path file : files) {
                long size = Files.size(file);
                int lines = (int) Files.lines(file).count();
                fileEntries.add(new FileEntry(file, lines, size));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void importSelected() {
        if (selectedIndex < 0 || selectedIndex >= fileEntries.size()) return;

        Path file = fileEntries.get(selectedIndex).path;

        new Thread(() -> {
            MessageImporter.ImportResult result = MessageImporter.importFromFile(file);
            Minecraft.getInstance().execute(() -> {
                if (result.success()) {
                    this.minecraft.setScreen(parentScreen);
                }
            });
        }, "AutoMessage-Import").start();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 背景
        renderBackground(graphics, mouseX, mouseY, partialTick);

        // 标题
        graphics.drawString(this.font, "选择要导入的TXT文件", 10, 10, 0xFFFFFF);

        // 路径
        String path = targetDir.toString();
        if (path.length() > 60) path = "..." + path.substring(path.length() - 57);
        graphics.drawString(this.font, path, 10, 25, 0xAAAAAA);

        // 计算列表区域（确保不覆盖按钮）
        int maxListHeight = this.height - LIST_BOTTOM_MARGIN - LIST_TOP;
        int visibleCount = Math.max(1, maxListHeight / ENTRY_HEIGHT);
        int listHeight = Math.min(fileEntries.size(), visibleCount) * ENTRY_HEIGHT;

        // 列表背景
        graphics.fill(10, LIST_TOP, this.width - 10, LIST_TOP + listHeight, 0xFF000000);

        // 绘制可见条目
        for (int i = 0; i < fileEntries.size(); i++) {
            int displayIndex = i - scrollOffset;
            if (displayIndex < 0 || displayIndex >= visibleCount) continue;

            int y = LIST_TOP + displayIndex * ENTRY_HEIGHT;
            FileEntry entry = fileEntries.get(i);

            boolean isSelected = (i == selectedIndex);
            boolean isHovered = mouseX >= 10 && mouseX <= this.width - 10
                    && mouseY >= y && mouseY < y + ENTRY_HEIGHT;

            // 背景
            int bgColor = isSelected ? 0xFF0066CC : (isHovered ? 0xFF444444 : 0xFF222222);
            graphics.fill(10, y, this.width - 10, y + ENTRY_HEIGHT - 1, bgColor);

            // ===== 文字绘制（最基础方式）=====
            String name = entry.path.getFileName().toString();
            if (name.length() > 40) name = name.substring(0, 37) + "...";

            // 文字Y坐标居中
            int textY = y + (ENTRY_HEIGHT - 8) / 2; // 8是字体高度

            // 白色文字，带阴影
            graphics.drawString(this.font, name, 15, textY, 0xFFFFFF, true);

            // 右侧显示行数
            String info = entry.lines + "行";
            int infoWidth = this.font.width(info);
            graphics.drawString(this.font, info, this.width - 15 - infoWidth, textY, 0xAAAAAA, true);
        }

        // 绘制滚动条指示器
        if (fileEntries.size() > visibleCount) {
            int barX = this.width - 12;
            int barHeight = listHeight * visibleCount / fileEntries.size();
            int barY = LIST_TOP + (scrollOffset * listHeight / fileEntries.size());
            graphics.fill(barX, barY, barX + 2, barY + barHeight, 0xFF888888);
        }

        // 数量提示
        if (fileEntries.isEmpty()) {
            graphics.drawString(this.font, "没有找到 .txt 文件", this.width / 2 - 50, LIST_TOP + 20, 0xFF5555);
            graphics.drawString(this.font, "点击'打开文件夹'添加文件", this.width / 2 - 60, LIST_TOP + 40, 0x888888);
        } else {
            graphics.drawString(this.font,
                    "共 " + fileEntries.size() + " 个文件" + (selectedIndex >= 0 ? " | 已选择: " + (selectedIndex + 1) : ""),
                    10, this.height - 55, 0x888888);
        }

        // 按钮由 super.render 绘制
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 检查是否点击列表区域
        int maxListHeight = this.height - LIST_BOTTOM_MARGIN - LIST_TOP;
        int visibleCount = Math.max(1, maxListHeight / ENTRY_HEIGHT);
        int listHeight = Math.min(fileEntries.size(), visibleCount) * ENTRY_HEIGHT;

        if (mouseX >= 10 && mouseX <= this.width - 10
                && mouseY >= LIST_TOP && mouseY < LIST_TOP + listHeight) {

            int displayIndex = (int) ((mouseY - LIST_TOP) / ENTRY_HEIGHT);
            int actualIndex = displayIndex + scrollOffset;

            if (actualIndex >= 0 && actualIndex < fileEntries.size()) {
                selectedIndex = actualIndex;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxListHeight = this.height - LIST_BOTTOM_MARGIN - LIST_TOP;
        int visibleCount = Math.max(1, maxListHeight / ENTRY_HEIGHT);

        if (fileEntries.size() > visibleCount) {
            int maxScroll = fileEntries.size() - visibleCount;
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - (int)scrollY));
        }
        return true;
    }

    private void openFolder() {
        try {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // 使用 explorer 直接打开，无需 rundll32
                new ProcessBuilder("explorer.exe", targetDir.toString()).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", targetDir.toString()).start();
            } else {
                // Linux: 尝试多种方式
                String[] commands = {"xdg-open", "nautilus", "dolphin", "thunar", "pcmanfm"};
                boolean opened = false;
                for (String cmd : commands) {
                    try {
                        new ProcessBuilder(cmd, targetDir.toString()).start();
                        opened = true;
                        break;
                    } catch (Exception ignored) {}
                }
                if (!opened) {
                    throw new RuntimeException("无法找到文件管理器");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 显示错误提示
            Minecraft.getInstance().gui.getChat().addMessage(
                    Component.literal("✗ 无法打开文件夹: " + e.getMessage())
                            .withStyle(ChatFormatting.RED)
            );
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parentScreen);
    }

    static class FileEntry {
        final Path path;
        final int lines;
        final long size;

        FileEntry(Path path, int lines, long size) {
            this.path = path;
            this.lines = lines;
            this.size = size;
        }
    }
}