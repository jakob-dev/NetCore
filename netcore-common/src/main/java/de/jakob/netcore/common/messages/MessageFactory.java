package de.jakob.netcore.common.messages;

import de.jakob.netcore.common.util.ChatFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class MessageFactory {

    private static BiFunction<UUID, String, String> placeholderParser = (uuid, text) -> text;

    public static void setPlaceholderParser(BiFunction<UUID, String, String> parser) {
        MessageFactory.placeholderParser = parser;
    }

    private String message;
    private final Map<String, String> replacements = new HashMap<>();
    private UUID targetUuid;

    public MessageFactory(String message) {
        this.message = message != null ? message : "???";
    }

    public static MessageFactory of(String message) {
        return new MessageFactory(message);
    }

    public MessageFactory target(UUID uuid) {
        this.targetUuid = uuid;
        return this;
    }

    public MessageFactory replace(String key, Object value) {
        this.replacements.put(key, String.valueOf(value));
        return this;
    }

    public MessageFactory replace(Map<String, String> values) {
        this.replacements.putAll(values);
        return this;
    }

    public MessageFactory prefix(String prefix) {
        if (prefix != null) {
            this.message = prefix + this.message;
        }
        return this;
    }

    public String build() {
        String result = message;

        for (Map.Entry<String, String> entry : replacements.entrySet()) {

            String key = entry.getKey();
            String val = entry.getValue();

            result = result.replace(key, val);
        }

        if (targetUuid != null) {
            result = placeholderParser.apply(targetUuid, result);
        }

        return ChatFormatter.translate(result);
    }
}
