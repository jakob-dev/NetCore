package de.jakob.netcore.api.database.queries;

import java.util.ArrayList;
import java.util.List;

public class DeleteQuery {

    private final String table;
    private final List<String> wheres = new ArrayList<String>();

    public DeleteQuery(String table) {
        this.table = table;
    }

    public DeleteQuery where(String expression) {
        wheres.add(expression);
        return this;
    }

    public DeleteQuery and(String expression) {
        where(expression);
        return this;
    }

    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ")
                .append(table);

        if (!wheres.isEmpty()) {
            builder.append(" WHERE ")
                    .append(String.join(" AND ", wheres));
        }

        return builder.toString();
    }

}
