package de.jakob.netcore.common.depend;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.UUID;

public class LuckPerms {

    public static boolean enabled;

    static {
        try {
            Class.forName("net.luckperms.api.LuckPerms");
            enabled = true;
        } catch (ClassNotFoundException e) {
            enabled = false;
        }
    }

    public static String getPrefix(UUID uuid) {
        if (!enabled) return "";
        try {
            net.luckperms.api.LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(uuid);
            if (user == null) return "";
            String prefix = user.getCachedData().getMetaData().getPrefix();
            return prefix != null ? prefix : "";
        } catch (Exception e) {
            return "";
        }
    }

    public static String getSuffix(UUID uuid) {
        if (!enabled) return "";
        try {
            net.luckperms.api.LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(uuid);
            if (user == null) return "";
            String suffix = user.getCachedData().getMetaData().getSuffix();
            return suffix != null ? suffix : "";
        } catch (Exception e) {
            return "";
        }
    }

    public static int getWeight(UUID uuid) {
        if (!enabled) return 0;
        try {
            net.luckperms.api.LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(uuid);
            if (user == null) return 0;
            String groupName = user.getPrimaryGroup();
            net.luckperms.api.model.group.Group group = lp.getGroupManager().getGroup(groupName);
            if (group != null) {
                return group.getWeight().orElse(0);
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
