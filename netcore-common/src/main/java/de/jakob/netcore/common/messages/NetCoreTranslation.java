package de.jakob.netcore.common.messages;

import java.util.function.Function;

public enum NetCoreTranslation {


    PREFIX("prefix"),
    NO_PERMISSION("no-permission"),
    ONLY_PLAYER("only-player"),
    CHAT_COOLDOWN("chat-cooldown"),
    SAME_MESSAGE("same-message"),
    NO_CAPS("no-caps"),
    PLAYTIME("playtime");

    final String key;

    NetCoreTranslation(String key) {
        this.key = key;
    }

    private static Function<String, String> translationProvider;

    public static void setTranslationProvider(Function<String, String> provider) {
        translationProvider = provider;
    }

    public String getTranslatedString() {
        return getMessageFactory().build();
    }

    public String getCompleteTranslatedString() {
        return PREFIX.getTranslatedString() + getMessageFactory().build();
    }

    public MessageFactory getMessageFactory() {
        String translated = null;
        if (translationProvider != null) {
            translated = translationProvider.apply(key);
        }

        if (translated == null) {
            translated = String.valueOf(key);
        }
        return MessageFactory.of(translated);
    }


}
