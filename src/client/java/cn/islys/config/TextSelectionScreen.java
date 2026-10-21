package cn.islys.config;

import cn.islys.util.MessageImporter;
import cn.islys.util.TextFileLibrary;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.LabelOption;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Resource-pack style selection semantics, entirely built with YACL controls. */
public final class TextSelectionScreen {
    private final Screen settingsParent;
    private final TextFileLibrary library = MessageImporter.getTextLibrary();
    private List<String> files = List.of();
    private Component status = text("hint");
    private boolean busy;
    private boolean closed;
    private Screen activeScreen;

    private TextSelectionScreen(Screen settingsParent) {
        this.settingsParent = settingsParent;
        refreshFiles();
    }

    public static Screen create(Screen settingsParent) {
        return new TextSelectionScreen(settingsParent).build();
    }

    private Screen build() {
        AutoMessengerConfig config = AutoMessengerConfig.getInstance();
        String selected = config.selectedTextFile;
        OptionGroup.Builder current = OptionGroup.createBuilder().name(text("current")).collapsed(false);
        current.option(LabelOption.create(selected.isEmpty()
                ? text("manual", config.scheduledMessageList.size())
                : text("selected", selected, config.scheduledMessageList.size())));
        if (!selected.isEmpty()) {
            if (!files.contains(selected)) current.option(LabelOption.create(text("missing").withStyle(ChatFormatting.YELLOW)));
            current.option(ButtonOption.createBuilder()
                    .name(Component.literal(selected))
                    .text(text("reload"))
                    .available(!busy && files.contains(selected))
                    .action((screen, option) -> select(selected))
                    .build());
        }

        OptionGroup.Builder available = OptionGroup.createBuilder().name(text("available")).collapsed(false);
        if (files.isEmpty()) available.option(LabelOption.create(text("empty")));
        for (String filename : files) {
            if (filename.equals(selected)) continue;
            available.option(ButtonOption.createBuilder()
                    .name(Component.literal(filename))
                    .text(text("use"))
                    .description(OptionDescription.of(text("use.desc")))
                    .available(!busy)
                    .action((screen, option) -> select(filename))
                    .build());
        }
        if (!files.isEmpty() && files.size() == 1 && files.contains(selected)) {
            available.option(LabelOption.create(text("all_selected")));
        }

        ConfigCategory category = ConfigCategory.createBuilder()
                .name(text("title"))
                .option(LabelOption.create(status))
                .option(LabelOption.create(text("directory", library.directory().toString())))
                .option(ButtonOption.createBuilder()
                        .name(text("open_folder"))
                        .text(text("open"))
                        .action((screen, option) -> openFolder())
                        .build())
                .option(ButtonOption.createBuilder()
                        .name(text("refresh"))
                        .text(text("refresh.button"))
                        .available(!busy)
                        .action((screen, option) -> {
                            status = text("hint");
                            refreshFiles();
                            show();
                        })
                        .build())
                .group(current.build())
                .group(available.build())
                .build();

        YetAnotherConfigLib yacl = YetAnotherConfigLib.createBuilder()
                .title(text("title"))
                .category(category)
                .save(() -> {})
                .build();
        activeScreen = new YACLScreen(yacl, null) {
            @Override
            public void onClose() {
                // Leaving during a read cancels that selection. Returning always rebuilds bindings.
                closed = true;
                Minecraft.getInstance().gui.setScreen(ModConfigScreen.create(settingsParent));
            }
        };
        return activeScreen;
    }

    private void select(String filename) {
        if (busy || closed) return;
        busy = true;
        status = text("loading", filename).withStyle(ChatFormatting.YELLOW);
        show();
        Minecraft client = Minecraft.getInstance();
        CompletableFuture.runAsync(() -> {
            try {
                TextFileLibrary.Content content = library.read(filename);
                client.execute(() -> {
                    if (!isCurrentScreen()) return;
                    MessageImporter.applyTextFile(filename, content);
                    busy = false;
                    status = text("success", filename, content.messages().size()).withStyle(ChatFormatting.GREEN);
                    refreshFiles();
                    show();
                });
            } catch (IOException | RuntimeException e) {
                client.execute(() -> {
                    if (!isCurrentScreen()) return;
                    busy = false;
                    status = text("failed", filename, e.getMessage()).withStyle(ChatFormatting.RED);
                    show();
                });
            }
        });
    }

    private void refreshFiles() {
        try {
            files = library.listFiles();
        } catch (IOException | SecurityException e) {
            files = List.of();
            status = text("scan_failed", e.getMessage()).withStyle(ChatFormatting.RED);
        }
    }

    private void openFolder() {
        try {
            library.ensureDirectory();
            Util.getPlatform().openPath(library.directory());
        } catch (IOException | RuntimeException e) {
            status = text("folder_failed", e.getMessage()).withStyle(ChatFormatting.RED);
            show();
        }
    }

    private void show() {
        if (!closed) Minecraft.getInstance().gui.setScreen(build());
    }

    private boolean isCurrentScreen() {
        // Disconnects and other game transitions may replace a screen without calling onClose().
        if (Minecraft.getInstance().gui.screen() != activeScreen) closed = true;
        return !closed;
    }

    private static net.minecraft.network.chat.MutableComponent text(String key, Object... args) {
        return Component.translatable("config.auto-messenger.txt." + key, args);
    }
}
