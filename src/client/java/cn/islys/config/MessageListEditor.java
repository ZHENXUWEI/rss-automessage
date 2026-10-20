package cn.islys.config;

import java.util.ArrayList;
import java.util.List;

/** Keeps the unshown suffix outside YACL's editable binding. */
final class MessageListEditor {
    private final boolean limited;
    private final List<String> hidden;

    MessageListEditor(List<String> messages, boolean showAll) {
        limited = messages.size() > 100 && !showAll;
        hidden = limited ? List.copyOf(messages.subList(100, messages.size())) : List.of();
    }

    List<String> visibleMessages(List<String> messages) {
        int count = limited ? Math.max(0, messages.size() - hidden.size()) : messages.size();
        return new ArrayList<>(messages.subList(0, count));
    }

    List<String> merge(List<String> visible) {
        List<String> result = new ArrayList<>(visible);
        result.addAll(hidden);
        return result;
    }
}
