package de.jakob.netcore.api.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseProvider {

    void connect();

    void disconnect();

    boolean isConnected();

    Connection getConnection() throws SQLException;

}
