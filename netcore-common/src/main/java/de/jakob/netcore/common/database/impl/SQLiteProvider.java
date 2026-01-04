package de.jakob.netcore.common.database.impl;

import com.zaxxer.hikari.HikariConfig;
import de.jakob.netcore.api.database.DatabaseSettings;
import de.jakob.netcore.common.database.AbstractDatabaseProvider;

import java.io.File;
import java.io.IOException;


public class SQLiteProvider extends AbstractDatabaseProvider {

    private final File file;

    public SQLiteProvider(DatabaseSettings settings, File dataFolder) {
        super(settings);
        this.file = new File(dataFolder, settings.database() + ".db");
    }

    @Override
    public void connect() {

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.connect();
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:sqlite:" + file.getAbsolutePath();
    }

    @Override
    protected String getDriverClass() {
        return "org.sqlite.JDBC";
    }

    @Override
    protected void applyConfigSettings(HikariConfig config) {
        config.setMaximumPoolSize(1);
    }
}
