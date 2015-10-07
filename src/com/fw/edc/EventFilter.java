package com.fw.edc;

import java.util.List;

/**
 * Created by mxu2 on 6/6/15.
 */
public class EventFilter {
    private String database;
    private String tableName;
    private String fieldName;

    public EventFilter() {
    }

    public EventFilter(String database, String tableName, String fieldName) {
        this.database = database;
        this.tableName = tableName;
        this.fieldName = fieldName;
    }

    public static List<EventFilter> getAllFilters() {
        return Config.getInstance().getIgnoreFilters();
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return database + '.' + tableName + '.' + fieldName;
    }
}
