package de.jakob.netcore.api.database.queries;

import java.util.ArrayList;
import java.util.List;

public class CreateTableQuery {

    private final String table;
    private boolean ifNotExists = false;
    private final List<String> columns = new ArrayList<String>();
    private String primaryKey;

    public CreateTableQuery(String table) {
        this.table = table;
    }

    public CreateTableQuery ifNotExists() {
        this.ifNotExists = true;
        return this;
    }

    public CreateTableQuery column(String column, String settings) {
        columns.add(column + " " + settings);
        return this;
    }

    public CreateTableQuery primaryKey(String column) {
        this.primaryKey = column;
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ");

        if (ifNotExists) {
            builder.append("IF NOT EXISTS ");
        }

        builder.append(table)
                .append(" (")
                .append(String.join(",", columns));

        if (primaryKey != null) {
            builder.append(",PRIMARY KEY(");
            builder.append(primaryKey);
            builder.append(")");
        }

        builder.append(")");

        return builder.toString();
    }

}
