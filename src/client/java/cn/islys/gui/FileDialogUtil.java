package cn.islys.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class FileDialogUtil {

    public static Optional<Path> showOpenDialog() {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                return windowsDialog();
            } else if (os.contains("mac")) {
                return macDialog();
            } else {
                return linuxDialog();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static Optional<Path> windowsDialog() throws Exception {
        Path configDir = cn.islys.util.MessageImporter.getConfigDir();

        // 方法1: 使用 explorer.exe 配合 PowerShell 的 Out-GridView（更可靠）
        // 方法2: 使用简单的 CMD 文件选择（通过临时脚本）

        // 创建临时 PowerShell 脚本文件（避免命令行转义问题）
        Path tempScript = Files.createTempFile("filechooser", ".ps1");
        String scriptContent = String.format(
                "Add-Type -AssemblyName System.Windows.Forms\n" +
                        "$f = New-Object System.Windows.Forms.OpenFileDialog\n" +
                        "$f.InitialDirectory = '%s'\n" +
                        "$f.Filter = 'TXT files (*.txt)|*.txt'\n" +
                        "$f.Title = '选择TXT文件'\n" +
                        "if ($f.ShowDialog() -eq 'OK') { Write-Output $f.FileName }\n",
                configDir.toString().replace("'", "''")
        );
        Files.writeString(tempScript, scriptContent);

        // 执行脚本
        ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe", "-ExecutionPolicy", "Bypass", "-File", tempScript.toString()
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        // 读取输出
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                output.append(line.trim());
            }
        }

        p.waitFor();
        Files.deleteIfExists(tempScript); // 清理临时文件

        String result = output.toString().trim();
        if (!result.isEmpty() && !result.equals("null") && !result.startsWith("New-Object")) {
            return Optional.of(Paths.get(result));
        }

        // 备用：如果 PowerShell 失败，使用简单的文件浏览器提示
        System.out.println("[AutoMessage] PowerShell dialog failed or cancelled, falling back to explorer");
        return Optional.empty();
    }

    private static Optional<Path> macDialog() throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "osascript", "-e",
                "try\n" +
                        "set theFile to choose file with prompt \"选择TXT文件\" of type {\"txt\"}\n" +
                        "POSIX path of theFile\n" +
                        "on error\n" +
                        "return \"\"\n" +
                        "end try"
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String result = reader.readLine();
        p.waitFor();

        if (result != null && !result.trim().isEmpty()) {
            return Optional.of(Paths.get(result.trim()));
        }
        return Optional.empty();
    }

    private static Optional<Path> linuxDialog() throws Exception {
        // 尝试多种对话框工具
        String[] commands = {
                "zenity --file-selection --file-filter=*.txt --title=\"选择TXT文件\" 2>/dev/null",
                "kdialog --getopenfilename . \"*.txt\" 2>/dev/null",
                "yad --file --file-filter=\"*.txt\" --title=\"选择TXT文件\" 2>/dev/null"
        };

        for (String cmd : commands) {
            try {
                ProcessBuilder pb = new ProcessBuilder("sh", "-c", cmd);
                pb.redirectErrorStream(true);
                Process p = pb.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String result = reader.readLine();
                p.waitFor();

                if (result != null && !result.trim().isEmpty() && Files.exists(Paths.get(result.trim()))) {
                    return Optional.of(Paths.get(result.trim()));
                }
            } catch (Exception ignored) {}
        }
        return Optional.empty();
    }
}