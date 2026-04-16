package cn.islys.gui;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class FileDialogUtil {

    /**
     * 修复版：使用 CompletableFuture 异步处理，避免阻塞
     */
    public static Optional<Path> showOpenDialog() {
        // 强制重置 headless
        System.setProperty("java.awt.headless", "false");

        CompletableFuture<Optional<Path>> future = new CompletableFuture<>();

        // 在全新线程中初始化 AWT
        Thread awtThread = new Thread(() -> {
            try {
                // 确保在新线程中初始化
                System.setProperty("java.awt.headless", "false");

                // 初始化图形环境
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

                Optional<Path> result = showAWTDialog();
                future.complete(result);

            } catch (Exception e) {
                System.err.println("[AutoMessage] AWT failed: " + e.getMessage());
                // 尝试 Swing
                try {
                    Optional<Path> result = showSwingDialog();
                    future.complete(result);
                } catch (Exception e2) {
                    future.complete(Optional.empty());
                }
            }
        }, "AWT-Dialog-Thread");

        awtThread.setDaemon(true);
        awtThread.start();

        // 等待结果（带超时）
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            return Optional.empty();
        }
    }

    private static Optional<Path> showAWTDialog() {
        // 使用 Dialog 而不是 Frame，避免窗口残留
        Dialog dialogHolder = new Dialog((Frame) null, "File Dialog Holder");
        dialogHolder.setUndecorated(true);
        dialogHolder.setModal(false);

        try {
            FileDialog fileDialog = new FileDialog(dialogHolder, "选择 TXT 文件", FileDialog.LOAD);
            fileDialog.setModal(true); // 模态对话框

            Path configDir = cn.islys.util.MessageImporter.getConfigDir();
            fileDialog.setDirectory(configDir.toString());
            fileDialog.setFile("*.txt");

            fileDialog.setVisible(true);

            String file = fileDialog.getFile();
            String dir = fileDialog.getDirectory();

            fileDialog.dispose();

            if (file != null) {
                return Optional.of(Paths.get(dir, file));
            }
        } finally {
            dialogHolder.dispose();
            // 强制垃圾回收，释放资源
            System.gc();
        }

        return Optional.empty();
    }

    private static Optional<Path> showSwingDialog() {
        // 在 EDT 中运行
        final Optional<Path>[] result = new Optional[1];

        try {
            SwingUtilities.invokeAndWait(() -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileFilter(new FileNameExtensionFilter("TXT 文件 (*.txt)", "txt"));

                Path configDir = cn.islys.util.MessageImporter.getConfigDir();
                chooser.setCurrentDirectory(configDir.toFile());
                chooser.setDialogTitle("选择 TXT 文件");

                int ret = chooser.showOpenDialog(null);

                if (ret == JFileChooser.APPROVE_OPTION) {
                    File selected = chooser.getSelectedFile();
                    result[0] = Optional.of(selected.toPath());
                } else {
                    result[0] = Optional.empty();
                }
            });
        } catch (Exception e) {
            result[0] = Optional.empty();
        }

        // 强制清理
        System.gc();

        return result[0];
    }
}