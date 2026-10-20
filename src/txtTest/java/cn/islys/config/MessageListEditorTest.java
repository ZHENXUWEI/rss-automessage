package cn.islys.config;

import java.util.List;
import java.util.stream.IntStream;

/** Regression: deleting visible messages and saving twice must not duplicate the hidden suffix. */
public final class MessageListEditorTest {
    public static void main(String[] args) {
        List<String> messages = IntStream.range(0, 150).mapToObj(i -> "message-" + i).toList();
        MessageListEditor editor = new MessageListEditor(messages, false);
        List<String> visible = editor.visibleMessages(messages);
        check(visible.size() == 100, "only 100 visible");
        visible.remove(0);
        List<String> saved = editor.merge(visible);
        check(saved.size() == 149, "deletion retains 50 hidden messages");
        check(editor.visibleMessages(saved).equals(visible), "getter matches pending after first save");
        visible = editor.visibleMessages(saved);
        visible.set(0, "edited");
        saved = editor.merge(visible);
        check(saved.size() == 149, "second save does not duplicate hidden messages");
        check(saved.subList(99, saved.size()).equals(messages.subList(100, 150)), "hidden suffix unchanged");
        check(editor.visibleMessages(saved).equals(visible), "getter still matches after second save");
        check(editor.merge(List.of()).equals(messages.subList(100, 150)), "deleting all visible messages retains hidden suffix");
        check(editor.visibleMessages(editor.merge(List.of())).isEmpty(), "empty visible prefix stays empty");

        MessageListEditor fullEditor = new MessageListEditor(messages, true);
        check(fullEditor.visibleMessages(messages).equals(messages), "show-all exposes every message");
        check(fullEditor.merge(List.of("replacement")).equals(List.of("replacement")), "show-all has no hidden suffix");
        MessageListEditor shortEditor = new MessageListEditor(List.of("one"), false);
        check(shortEditor.merge(List.of("two")).equals(List.of("two")), "short lists edit normally");
        System.out.println("PASS: 11 message-list editor checks");
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
    }
}
