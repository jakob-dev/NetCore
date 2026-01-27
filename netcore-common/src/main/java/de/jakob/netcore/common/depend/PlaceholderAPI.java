package de.jakob.netcore.common.depend;

public class PlaceholderAPI {

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
