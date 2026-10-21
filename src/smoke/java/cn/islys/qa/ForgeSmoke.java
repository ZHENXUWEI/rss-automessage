package cn.islys.qa;

import cn.islys.RSsAutoMessageClient;
import cn.islys.config.AutoMessengerConfig;
import cn.islys.util.MessageImporter;
import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/** Disposable, run-only integration mod. Never included in the shipped mod JAR. */
@Mod("rss_automessage_qa")
public final class ForgeSmoke {
    private int phase;
    private int ticks;
    private int checks;
    private int closedTick;
    private long deadline = System.currentTimeMillis() + 180000;
    private Screen busyScreen;
    private boolean readPoolDrained;

    public ForgeSmoke() {
        ClientTickEvent.Post.BUS.addListener(this::tick);
        System.out.println("FORGE_SMOKE_REGISTERED");
    }

    private void tick(ClientTickEvent.Post event) {
        try {
            ticks++;
            if (ticks == 1) System.out.println("FORGE_SMOKE_FIRST_TICK");
            if (System.currentTimeMillis() > deadline) throw new AssertionError("Smoke timeout in phase " + phase);
            if (phase == 0) {
                if (ticks < 20 || screen() == null || overlay() != null) return;
                check(RSsAutoMessageClient.getInstance() != null, "client-only entry point initialized");
                String modSource = RSsAutoMessageClient.class.getProtectionDomain().getCodeSource().getLocation().toString();
                String yaclSource = dev.isxander.yacl3.api.YetAnotherConfigLib.class.getProtectionDomain().getCodeSource().getLocation().toString();
                System.out.println("FORGE_SMOKE_ORIGINS mod=" + modSource + " yacl=" + yaclSource);
                check(modSource.contains("rss-automessage-forge-") && modSource.contains(".jar"),
                        "AutoMessage loaded from the release JAR");
                check(yaclSource.contains("yacl-reforged-") && yaclSource.contains(".jar"),
                        "YACL loaded from the embedded mod without standalone classpath dependency");
                Path directory = MessageImporter.getTextLibrary().directory();
                check(Files.isDirectory(directory), "TXT directory created");
                check(MessageImporter.getTextLibrary().listFiles().isEmpty(), "TXT directory initially empty");
                AutoMessengerConfig config = AutoMessengerConfig.getInstance();
                check(!config.enableScheduledMessages && !config.sendOnJoin && !config.enableAutoReply,
                        "isolated QA profile has sending disabled");
                Files.writeString(directory.resolve("alpha.txt"), "# alpha fixture\nalpha message\n/warp alpha\n");
                Files.writeString(directory.resolve("beta.txt"), "beta message\n");
                Files.writeString(directory.resolve("empty.txt"), "# only comments\n");
                var container = ModList.getModContainerById("rss_automessage").orElseThrow();
                var factory = container.getCustomExtension(ConfigScreenFactory.class).orElseThrow();
                Screen settings = factory.screenFunction().apply(Minecraft.getInstance(), screen());
                check(settings instanceof YACLScreen, "native Mods config factory creates YACL screen");
                open(settings);
                pressByKey("config.auto-messenger.txt.choose");
                check(screen() instanceof YACLScreen && screen() != settings, "TXT chooser opens through settings button");
                press("alpha.txt");
                step(1);
            } else if (phase == 1) {
                if (!AutoMessengerConfig.getInstance().selectedTextFile.equals("alpha.txt")) return;
                check(AutoMessengerConfig.getInstance().scheduledMessageList.equals(List.of("alpha message|msg", "warp alpha|cmd")),
                        "TXT message and command imported");
                check(RSsAutoMessageClient.getInstance().getRemainingMs() == AutoMessengerConfig.getInstance().sendIntervalMs,
                        "successful selection resets countdown");
                press("beta.txt");
                step(2);
            } else if (phase == 2) {
                if (!AutoMessengerConfig.getInstance().selectedTextFile.equals("beta.txt")) return;
                check(AutoMessengerConfig.getInstance().scheduledMessageList.equals(List.of("beta message|msg")),
                        "second text replaces previous messages");
                Files.writeString(MessageImporter.getTextLibrary().directory().resolve("beta.txt"), "reloaded beta\n");
                press("beta.txt");
                step(3);
            } else if (phase == 3) {
                if (!AutoMessengerConfig.getInstance().scheduledMessageList.equals(List.of("reloaded beta|msg"))) return;
                check(true, "active file reloads changed content");
                press("empty.txt");
                busyScreen = screen();
                step(4);
            } else if (phase == 4) {
                if (screen() == busyScreen || !button("empty.txt").available()) return;
                check(AutoMessengerConfig.getInstance().selectedTextFile.equals("beta.txt"), "empty text keeps previous selection");
                check(AutoMessengerConfig.getInstance().scheduledMessageList.equals(List.of("reloaded beta|msg")),
                        "empty text keeps previous messages");
                press("alpha.txt");
                screen().onClose();
                closedTick = ticks;
                step(5);
            } else if (phase == 5) {
                if (!readPoolDrained) {
                    check(ForkJoinPool.commonPool().awaitQuiescence(5, TimeUnit.SECONDS),
                            "async read pool drained after closing selector");
                    readPoolDrained = true;
                    closedTick = ticks;
                    return;
                }
                if (ticks - closedTick < 10) return;
                check(AutoMessengerConfig.getInstance().selectedTextFile.equals("beta.txt"), "leaving during read cancels selection");
                check(AutoMessengerConfig.getInstance().scheduledMessageList.equals(List.of("reloaded beta|msg")),
                        "cancelled read does not change messages");
                check(screen() instanceof YACLScreen, "closing TXT chooser returns to YACL settings");
                YACLScreen settings = (YACLScreen) screen();
                @SuppressWarnings("unchecked")
                Option<Boolean> display = (Option<Boolean>) options(settings).stream()
                        .filter(option -> key(option).equals("config.auto-messenger.show_countdown")).findFirst().orElseThrow();
                display.requestSet(!display.pendingValue());
                settings.finishOrSave();
                check(AutoMessengerConfig.getInstance().scheduledMessageList.equals(List.of("reloaded beta|msg")),
                        "saving rebuilt settings preserves imported messages");
                AutoMessengerConfig.load();
                check(AutoMessengerConfig.getInstance().selectedTextFile.equals("beta.txt"), "selected filename persists on config reload");
                check(AutoMessengerConfig.getInstance().scheduledMessageList.equals(List.of("reloaded beta|msg")),
                        "message snapshot persists on config reload");
                String json = Files.readString(FMLPaths.CONFIGDIR.get().resolve("rss-automessage.json"));
                check(json.contains("beta.txt") && json.contains("reloaded beta|msg"), "selected text saved to expected config path");
                Files.writeString(FMLPaths.GAMEDIR.get().resolve("forge-smoke-result.json"),
                        "{\"passed\":true,\"checks\":" + checks + "}");
                System.out.println("FORGE_SMOKE_PASS checks=" + checks);
                phase = 6;
                Minecraft.getInstance().stop();
            }
        } catch (Throwable failure) {
            System.err.println("FORGE_SMOKE_FAIL phase=" + phase + " " + failure);
            failure.printStackTrace();
            throw new RuntimeException("Forge smoke test failed", failure);
        }
    }

    private void step(int next) { phase = next; deadline = System.currentTimeMillis() + 15000; }
    private void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
        System.out.println("FORGE_SMOKE_CHECK " + message);
    }
    private List<Option<?>> options(YACLScreen screen) {
        return screen.config.categories().stream().flatMap(category -> category.groups().stream())
                .flatMap(group -> group.options().stream()).<Option<?>>map(option -> option).toList();
    }
    private String key(Option<?> option) {
        return option.name().getContents() instanceof TranslatableContents text ? text.getKey() : "";
    }
    private ButtonOption button(String name) {
        return options((YACLScreen) screen()).stream().filter(option -> option instanceof ButtonOption)
                .map(option -> (ButtonOption) option).filter(option -> option.name().getString().equals(name))
                .findFirst().orElseThrow(() -> new AssertionError("No button: " + name));
    }
    private void press(String name) { YACLScreen current = (YACLScreen) screen(); ButtonOption button = button(name); button.action().accept(current, button); }
    private void pressByKey(String key) {
        YACLScreen current = (YACLScreen) screen();
        ButtonOption button = options(current).stream().filter(option -> option instanceof ButtonOption && key(option).equals(key))
                .map(option -> (ButtonOption) option).findFirst().orElseThrow();
        button.action().accept(current, button);
    }
    private Screen screen() {
        Minecraft client = Minecraft.getInstance();
        try { return (Screen) Minecraft.class.getField("screen").get(client); }
        catch (NoSuchFieldException ignored) {
            try { return (Screen) client.gui.getClass().getMethod("screen").invoke(client.gui); }
            catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
        } catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
    }
    private Object overlay() {
        Minecraft client = Minecraft.getInstance();
        try { return Minecraft.class.getMethod("getOverlay").invoke(client); }
        catch (NoSuchMethodException ignored) {
            try { return client.gui.getClass().getMethod("overlay").invoke(client.gui); }
            catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
        } catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
    }
    private void open(Screen screen) {
        Minecraft client = Minecraft.getInstance();
        try { Minecraft.class.getMethod("setScreen", Screen.class).invoke(client, screen); }
        catch (NoSuchMethodException ignored) {
            try { client.gui.getClass().getMethod("setScreen", Screen.class).invoke(client.gui, screen); }
            catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
        } catch (ReflectiveOperationException error) { throw new RuntimeException(error); }
    }
}
