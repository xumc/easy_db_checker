package com.fw.edc;

import com.github.shyiko.mysql.binlog.event.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by mxu2 on 5/31/15.
 */
public class PackagedEvent {
    private EventType type;
    private String database;
    private String tableName;
    private String entryName;
    private String entryId;
    private List<List<Field>> recordFields = new ArrayList<List<Field>>();

    public PackagedEvent(List<Event> events) throws Exception {
        Event mainEvent = events.get(events.size() - 1);
        this.type = mainEvent.getHeader().getEventType();

        TableMapEventData tableMap = (TableMapEventData) events.get(events.size() - 2).getData();
        this.database = tableMap.getDatabase();
        this.tableName = tableMap.getTable();

        List<MysqlField> fields = MysqlMetadata.getInstance().getFieldList(database, this.tableName);
        if (EventType.isUpdate(this.type)) {
            UpdateRowsEventData updateData = (UpdateRowsEventData) mainEvent.getData();
            for (Map.Entry<Serializable[], Serializable[]> row : updateData.getRows()) {
                Serializable[] oldValues = row.getKey();
                Serializable[] newValues = row.getValue();
                List<Field> recordField = new ArrayList<Field>();
                for (int i = 0; i < fields.size(); i++) {
                    MysqlField mField = fields.get(i);
                    Serializable oldValue = oldValues[i];
                    Serializable newValue = newValues[i];
                    if (mField.isEnumType()) {
                        oldValue = mField.enumConvert(oldValue);
                        newValue = mField.enumConvert(newValue);
                    }
                    Field field = new Field(mField.getName(), oldValue, newValue);
                    recordField.add(field);
                }
                recordFields.add(recordField);
            }
        } else if (EventType.isWrite(this.type)) {
            WriteRowsEventData writeData = (WriteRowsEventData) mainEvent.getData();
            for (Serializable[] row : writeData.getRows()) {
                List<Field> recordField = new ArrayList<Field>();
                for (int i = 0; i < fields.size(); i++) {
                    MysqlField mField = fields.get(i);
                    Serializable insertValue = row[i];
                    if (mField.isEnumType()) {
                        insertValue = mField.enumConvert(insertValue);
                    }
                    Field field = new Field(mField.getName(), "", insertValue);
                    recordField.add(field);
                }
                recordFields.add(recordField);
            }
        } else if (EventType.isDelete(this.type)) {
            DeleteRowsEventData deleteData = (DeleteRowsEventData) mainEvent.getData();
            for (Serializable[] row : deleteData.getRows()) {
                List<Field> recordField = new ArrayList<Field>();
                for (int i = 0; i < fields.size(); i++) {
                    MysqlField mField = fields.get(i);
                    Serializable deleteValue = row[i];
                    if (mField.isEnumType()) {
                        deleteValue = mField.enumConvert(deleteValue);
                    }
                    Field field = new Field(mField.getName(), deleteValue, "");
                    recordField.add(field);
                }
                recordFields.add(recordField);
            }
        }
    }

    public PackagedEvent() {

    }

    public static String showType(EventType type) {
        if (EventType.isUpdate(type)) {
            return "UPDATE";
        }
        if (EventType.isWrite(type)) {
            return "INSERT";
        }
        if (EventType.isDelete(type)) {
            return "DELETE";
        }
        return "";
    }

    public static EventType convertType(String type) {
        if ("INSERT".equals(type)) {
            return EventType.WRITE_ROWS;
        } else if ("UPDATE".equals(type)) {
            return EventType.UPDATE_ROWS;
        } else if ("DELETE".equals(type)) {
            return EventType.DELETE_ROWS;
        }
        return null;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<List<Field>> getRecordFields() {
        return recordFields;
    }

    public void setRecordFields(List<List<Field>> recordFields) {
        this.recordFields = recordFields;
    }

    public boolean filterOutBy(List<EventFilter> eventFilters) {
        for (EventFilter ef : eventFilters) {
            if (!this.database.equals(ef.getDatabase()) && !"*".equals(ef.getDatabase())) {
                continue;
            } else {
                if (!this.tableName.equals(ef.getTableName()) && !"*".equals(ef.getTableName())) {
                    continue;
                } else {
                    if ("*".equals(ef.getFieldName())) {
                        return true;
                    } else {
                        for (List<Field> recordField : recordFields) {
                            for (Field field : recordField) {
                                boolean otherUpdated = false;
                                for (Field field1 : recordField) {
                                    if (!field1.name.equals(ef.getFieldName()) && field1.updated) {
                                        otherUpdated = true;
                                    }
                                }
                                if (field.name.equals(ef.getFieldName()) && field.updated && !otherUpdated) {
                                    return true;
                                }
                            }
                        }
                    }

                }
            }
        }
        return false;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    static class Field {
        String name;
        Serializable oldValue;
        Serializable newValue;
        boolean updated = false;

        public Field() {
        }

        public Field(String name, Serializable oldValue, Serializable newValue) {
            this.name = name;
            this.oldValue = oldValue;
            this.newValue = newValue;
            if (oldValue == null) {
                if (newValue != null) {
                    this.updated = true;
                }
            } else {
                if (newValue == null) {
                    this.updated = true;
                } else {
                    if (!oldValue.toString().equals(newValue.toString())) {
                        this.updated = true;
                    }
                }
            }
        }

    }
}
