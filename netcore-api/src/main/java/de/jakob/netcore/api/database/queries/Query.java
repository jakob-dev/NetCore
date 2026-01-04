package de.jakob.netcore.api.database.queries;

import de.jakob.netcore.api.database.DatabaseProvider;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Query {

    private final DatabaseProvider provider;
    private final String sql;

    private final Map<Integer, Object> currentParameters = new HashMap<>();
    private final List<Map<Integer, Object>> batchParameters = new ArrayList<>();

    public Query(DatabaseProvider provider, String sql) {
        this.provider = provider;
        this.sql = sql;
    }

    public Query(DatabaseProvider provider, QueryBuilder builder) {
        this(provider, builder.build());
    }

    public Query setParameter(int index, Object value) {
        currentParameters.put(index, value);
        return this;
    }

    public void addBatch() {
        batchParameters.add(new HashMap<>(currentParameters));
        currentParameters.clear();
    }

    public int executeUpdate() throws SQLException {

        try (Connection connection = provider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            applyParameters(statement, currentParameters);
            return statement.executeUpdate();
        }
    }

    public ResultSet executeQuery() throws SQLException {

        RowSetFactory rowSetFactory = RowSetProvider.newFactory();
        CachedRowSet rowSet = rowSetFactory.createCachedRowSet();

        try (Connection connection = provider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            applyParameters(statement, currentParameters);

            try (ResultSet resultSet = statement.executeQuery()) {
                rowSet.populate(resultSet);
            }
        }
        return rowSet;
    }

    public int[] executeBatch() throws SQLException {
        try (Connection connection = provider.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {

                for (Map<Integer, Object> params : batchParameters) {
                    applyParameters(statement, params);
                    statement.addBatch();
                }

                if (!currentParameters.isEmpty()) {
                    applyParameters(statement, currentParameters);
                    statement.addBatch();
                }

                int[] results = statement.executeBatch();
                connection.commit();
                return results;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public CompletableFuture<Integer> executeUpdateAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<ResultSet> executeQueryAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeQuery();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<int[]> executeBatchAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeBatch();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void applyParameters(PreparedStatement statement, Map<Integer, Object> params) throws SQLException {
        for (Map.Entry<Integer, Object> entry : params.entrySet()) {
            statement.setObject(entry.getKey(), entry.getValue());
        }
    }

}
