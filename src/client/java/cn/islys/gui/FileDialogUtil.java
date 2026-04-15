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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class FileDialogUtil {

    /**
     * 显示文件选择对话框（强制修复 Headless 模式）
     */
    public static Optional<Path> showOpenDialog() {
        // 强制重置 headless 属性（必须在任何 AWT 类加载之前）
        System.setProperty("java.awt.headless", "false");

        // 使用 CountDownLatch 确保同步等待
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Optional<Path>> result = new AtomicReference<>(Optional.empty());

        // 在新线程中运行，避免阻塞 Minecraft 主线程
        Thread dialogThread = new Thread(() -> {
            try {
                // 再次强制设置
                System.setProperty("java.awt.headless", "false");

                // 尝试初始化图形环境
                GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

                Optional<Path> path = showDialogInternal();
                result.set(path);
            } catch (Exception e) {
                System.err.println("[AutoMessage] Dialog error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }, "FileDialog-Thread");

        // 设置为守护线程
        dialogThread.setDaemon(true);
        dialogThread.start();

        // 等待对话框关闭（最多等待60秒）
        try {
            latch.await(60, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result.get();
    }

    private static Optional<Path> showDialogInternal() {
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

    private static Optional<Path> showAWTDialog() {
        Frame frame = new Frame();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);

        try {
            FileDialog dialog = new FileDialog(frame, "Select TXT File", FileDialog.LOAD);

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

    private static Optional<Path> showSwingDialog() {
        try {
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("TXT Files (*.txt)", "txt"));

        Path configDir = cn.islys.util.MessageImporter.getConfigDir();
        chooser.setCurrentDirectory(configDir.toFile());
        chooser.setDialogTitle("Select TXT File");

        int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            return Optional.of(selected.toPath());
        }

        return Optional.empty();
    }
}