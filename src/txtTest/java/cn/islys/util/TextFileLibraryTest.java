package cn.islys.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/** Run with Gradle txtLibraryTest or Java 21; no game instance or test framework is required. */
public final class TextFileLibraryTest {
    private static int passed;

    public static void main(String[] args) throws Exception {
        Path gameDir = Files.createTempDirectory("automessage-library-test-");
        try {
            TextFileLibrary library = new TextFileLibrary(gameDir);
            library.ensureDirectory();
            check(library.directory().equals(gameDir.resolve("rss-automessage")), "game-root directory");
            check(library.listFiles().isEmpty(), "initial directory is empty");
            library.ensureDirectory();
            check(library.listFiles().isEmpty(), "initialization is idempotent");

            Path directory = library.directory();
            Files.writeString(directory.resolve("b.TXT"), "second");
            Files.writeString(directory.resolve("A.txt"), "first");
            Files.writeString(directory.resolve("ignored.json"), "ignored");
            Files.createDirectory(directory.resolve("folder.txt"));
            Files.writeString(directory.resolve("folder.txt/nested.txt"), "nested");
            check(library.listFiles().equals(List.of("A.txt", "b.TXT")), "direct regular TXT files, sorted without case");

            Files.writeString(directory.resolve("中文.txt"), "\uFEFF# 注释\n// comment\n\n  欢迎！  \n /warp spawn \n/\n", StandardCharsets.UTF_8);
            TextFileLibrary.Content content = library.read("中文.txt");
            check(content.messages().equals(List.of("欢迎！|msg", "warp spawn|cmd")), "UTF-8 BOM, comments, commands and whitespace");
            check(content.commandCount() == 1 && content.commentCount() == 2, "message statistics");
            Files.writeString(directory.resolve("A.txt"), "changed");
            check(library.read("A.txt").messages().equals(List.of("changed|msg")), "reload reads latest content");

            Files.writeString(directory.resolve("empty.txt"), "# comment only\n\n// ignored\n");
            expectFailure(() -> library.read("empty.txt"), "empty messages rejected");
            expectFailure(() -> library.read("missing.txt"), "deleted/missing file rejected");
            Files.write(directory.resolve("bad.txt"), new byte[]{(byte) 0xC3, (byte) 0x28});
            expectFailure(() -> library.read("bad.txt"), "invalid UTF-8 rejected");
            expectFailure(() -> library.read("../outside.txt"), "parent traversal rejected");
            expectFailure(() -> library.read(gameDir.resolve("outside.txt").toString()), "absolute path rejected");
            expectFailure(() -> library.read("ignored.json"), "non-TXT rejected");
            expectFailure(() -> library.read("folder.txt"), "directory rejected");
            check(content.messages().equals(List.of("欢迎！|msg", "warp spawn|cmd")), "failed reads preserve previous snapshot");
            try {
                content.messages().add("mutation");
                throw new AssertionError("snapshot must be immutable");
            } catch (UnsupportedOperationException expected) {
                passed++;
            }
            System.out.println("PASS: " + passed + " TXT library checks");
        } finally {
            try (var files = Files.walk(gameDir)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        passed++;
    }

    private static void expectFailure(IoAction action, String name) throws Exception {
        try {
            action.run();
            throw new AssertionError(name);
        } catch (IOException expected) {
            passed++;
        }
    }

    private interface IoAction {
        void run() throws Exception;
    }
}
