package cn.islys.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** A single, game-local folder of message files. Reading never changes live configuration. */
public final class TextFileLibrary {
    private final Path directory;

    public TextFileLibrary(Path gameDirectory) {
        directory = gameDirectory.resolve("rss-automessage").toAbsolutePath().normalize();
    }

    public Path directory() {
        return directory;
    }

    public void ensureDirectory() throws IOException {
        Files.createDirectories(directory);
    }

    public List<String> listFiles() throws IOException {
        ensureDirectory();
        try (var paths = Files.list(directory)) {
            return paths.filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                    .map(path -> path.getFileName().toString())
                    .filter(TextFileLibrary::isTxt)
                    .sorted(String.CASE_INSENSITIVE_ORDER.thenComparing(Comparator.naturalOrder()))
                    .toList();
        }
    }

    public Content read(String filename) throws IOException {
        try {
            Path path = directory.resolve(filename).normalize();
            if (filename.isBlank() || !isTxt(filename) || Path.of(filename).isAbsolute()
                    || !directory.equals(path.getParent()) || !path.getFileName().toString().equals(filename)
                    || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                throw new IOException("Not an available TXT file: " + filename);
            }
            return readFile(path);
        } catch (InvalidPathException e) {
            throw new IOException("Invalid TXT filename", e);
        }
    }

    /** Also used by the legacy explicit-path importer. */
    public static Content readFile(Path file) throws IOException {
        List<String> messages = new ArrayList<>();
        int commands = 0;
        int comments = 0;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine && line.startsWith("\uFEFF")) line = line.substring(1);
                firstLine = false;
                String trimmed = line.trim();
                if (trimmed.startsWith("#") || trimmed.startsWith("//")) comments++;
                String parsed = parseLine(line);
                if (parsed != null) {
                    messages.add(parsed);
                    if (parsed.endsWith("|cmd")) commands++;
                }
            }
        }
        if (messages.isEmpty()) throw new IOException("No valid messages in " + file.getFileName());
        return new Content(messages, commands, comments);
    }

    public static String parseLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) return null;
        if (trimmed.startsWith("/")) {
            String command = trimmed.substring(1).trim();
            return command.isEmpty() ? null : command + "|cmd";
        }
        return trimmed + "|msg";
    }

    private static boolean isTxt(String filename) {
        return filename.toLowerCase(Locale.ROOT).endsWith(".txt");
    }

    public record Content(List<String> messages, int commandCount, int commentCount) {
        public Content {
            messages = List.copyOf(messages);
        }
    }
}
