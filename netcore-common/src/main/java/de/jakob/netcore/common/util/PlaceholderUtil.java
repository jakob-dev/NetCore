package de.jakob.netcore.common.util;

public class PlaceholderUtil {

    public static boolean enabled;

    static {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            enabled = true;
        } catch (ClassNotFoundException e) {
            enabled = false;
        }
    }

}
