package de.jakob.netcore.common.database.impl;

import com.zaxxer.hikari.HikariConfig;
import de.jakob.netcore.api.database.DatabaseSettings;
import de.jakob.netcore.common.database.NetCoreDatabaseProvider;

import java.io.File;
import java.io.IOException;

public class H2Provider extends NetCoreDatabaseProvider {

    private final File file;

    public H2Provider(DatabaseSettings settings, String dataFolderPath) {
        super(settings);
        this.file = new File(dataFolderPath, settings.database() + ".db");
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
        return "jdbc:h2:" + file.getAbsolutePath();
    }

    @Override
    protected String getDriverClass() {
        return "org.h2.Driver";
    }

    @Override
    protected void applyConfigSettings(HikariConfig config) {
        config.setMaximumPoolSize(1);
    }
}
