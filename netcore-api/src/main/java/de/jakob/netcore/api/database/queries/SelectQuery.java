package de.jakob.netcore.api.database.queries;

import java.util.ArrayList;
import java.util.List;

public class SelectQuery {

    private final String table;
    private final List<String> columns = new ArrayList<String>();
    private final List<String> wheres = new ArrayList<String>();
    private String orderBy;
    private boolean orderByAscending = false;
    private int limitOffset = 0;
    private int limitRowCount = 0;

    public SelectQuery(String table) {
        this.table = table;
    }

    public SelectQuery column(String column) {
        columns.add(column);
        return this;
    }

    public SelectQuery where(String expression) {
        wheres.add(expression);
        return this;
    }

    public SelectQuery and(String expression) {
        where(expression);
        return this;
    }

    public SelectQuery orderBy(String column, boolean ascending) {
        this.orderBy = column;
        this.orderByAscending = ascending;
        return this;
    }

    public SelectQuery limit(int offset, int rowCount) {
        this.limitOffset = offset;
        this.limitRowCount = rowCount;
        return this;
    }

    public SelectQuery limit(int rowCount) {
        this.limitOffset = 0;
        this.limitRowCount = rowCount;
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT ")
                .append(String.join(",", columns))
                .append(" FROM ")
                .append(table);

        if (!wheres.isEmpty()) {
            builder.append(" WHERE ")
                    .append(String.join(" AND ", wheres));
        }

        if (orderBy != null) {
            builder.append(" ORDER BY ")
                    .append(orderBy)
                    .append(orderByAscending ? " ASC" : " DESC");
        }

        if (limitRowCount > 0) {
            builder.append(" LIMIT ")
                    .append(limitOffset)
                    .append(",").append(limitRowCount);
        }

        return builder.toString();
    }

}
