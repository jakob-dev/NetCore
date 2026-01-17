package de.jakob.netcore.common.database.provider;

import com.zaxxer.hikari.HikariConfig;
import de.jakob.netcore.api.database.DatabaseSettings;
import de.jakob.netcore.common.database.HikariDatabaseProvider;

public class  MariaDBProvider extends HikariDatabaseProvider {

    public MariaDBProvider(DatabaseSettings settings) {
        super(settings);
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:mariadb://" + settings.host() + ":" + settings.port() + "/" + settings.database() + "?useSSL=false";
    }

    @Override
    protected String getDriverClass() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    protected void applyConfigSettings(HikariConfig config) {
        config.setUsername(settings.username());
        config.setPassword(settings.password());
    }
}
