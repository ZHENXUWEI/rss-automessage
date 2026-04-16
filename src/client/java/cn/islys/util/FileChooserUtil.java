package cn.islys.util;

import net.minecraft.client.Minecraft;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import java.nio.file.Path;
import java.util.Optional;

public class FileChooserUtil {

    // MC 1.21 专用 → 不报错、不Headless、原生文件选择框
    public static Optional<Path> openTxtFileChooser() {
        try {
            // 直接调用，不做线程判断（你已经在外层开了新线程）
            String path = TinyFileDialogs.tinyfd_openFileDialog(
                    "选择TXT文件",
                    Minecraft.getInstance().gameDirectory.getAbsolutePath(),
                    null,
                    "文本文档 (*.txt)",
                    false
            );

            if (path == null || path.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(Path.of(path));
        } catch (Exception e) {
            // 绝对不会让游戏崩溃
            e.printStackTrace();
            return Optional.empty();
        }
    }
}