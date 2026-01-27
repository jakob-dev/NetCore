package de.jakob.netcore.spigot.config;

import de.jakob.netcore.spigot.NetCore;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class TranslationConfig {

    private final NetCore plugin;
    private File file;
    private YamlConfiguration config;

    public TranslationConfig(NetCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "translation.yml");

        if (!file.exists())
            plugin.saveResource("translation.yml", false);

        config = new YamlConfiguration();

        try {
            config.options().parseComments(true);
        } catch (final Throwable t) {
            // Unsupported
        }

        try {
            config.load(file);

        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

}
