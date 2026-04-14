package cn.islys.gui;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileDialogUtil {

    // 静态初始化块：强制启用 AWT 图形模式
    static {
        try {
            // 强制设置 AWT 为非 Headless 模式
            System.setProperty("java.awt.headless", "false");

            // 初始化图形环境
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println("[AutoMessage] Warning: GraphicsEnvironment is headless!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 主方法：尝试使用 AWT FileDialog，失败则使用 Swing
     */
    public static Optional<Path> showOpenDialog() {
        // 确保在主线程或 EDT 中运行
        if (java.awt.EventQueue.isDispatchThread()) {
            return showDialogInternal();
        }

        // 使用 invokeAndWait 确保在 EDT 中同步执行
        final Optional<Path>[] result = new Optional[1];
        try {
            java.awt.EventQueue.invokeAndWait(() -> {
                result[0] = showDialogInternal();
            });
            return result[0];
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static Optional<Path> showDialogInternal() {
        // 再次检查并尝试修复 Headless 模式
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("[AutoMessage] Cannot show dialog: Headless mode");
            return Optional.empty();
        }

        // 首先尝试 AWT FileDialog
        try {
            return showAWTDialog();
        } catch (Exception e) {
            System.out.println("[AutoMessage] AWT dialog failed: " + e.getMessage());
        }

        // 备用：Swing JFileChooser
        try {
            return showSwingDialog();
        } catch (Exception e) {
            System.err.println("[AutoMessage] Swing dialog also failed: " + e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * AWT FileDialog - 系统原生外观
     */
    private static Optional<Path> showAWTDialog() {
        // 使用 Minecraft 窗口作为父组件（如果可能）
        Frame frame = new Frame();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true); // 确保在最前

        try {
            FileDialog dialog = new FileDialog(frame, "选择 TXT 文件", FileDialog.LOAD);

            Path configDir = cn.islys.util.MessageImporter.getConfigDir();
            dialog.setDirectory(configDir.toString());
            dialog.setFile("*.txt");

            dialog.setVisible(true);

            String file = dialog.getFile();
            String dir = dialog.getDirectory();

            if (file != null) {
                return Optional.of(Paths.get(dir, file));
            }
        } finally {
            frame.dispose();
        }

        return Optional.empty();
    }

    /**
     * Swing JFileChooser - 备用方案
     */
    private static Optional<Path> showSwingDialog() {
        // 设置系统外观
        try {
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("TXT 文件 (*.txt)", "txt"));

        Path configDir = cn.islys.util.MessageImporter.getConfigDir();
        chooser.setCurrentDirectory(configDir.toFile());
        chooser.setDialogTitle("选择 TXT 文件");

        int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            return Optional.of(selected.toPath());
        }

        return Optional.empty();
    }
}