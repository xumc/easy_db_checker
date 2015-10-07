package com.fw.edc;

import java.io.Serializable;

/**
 * Created by mxu2 on 7/31/15.
 */
public class MysqlField {
    private String database;
    private String table;
    private String name;
    private String type;


    public MysqlField(String database, String table, String name, String type) {
        this.database = database;
        this.table = table;
        this.name = name;
        this.type = type;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isEnumType() {
        return type.startsWith("enum");
    }

    public Serializable enumConvert(Serializable sIndex) {
        if (sIndex == null) {
            return sIndex;
        }
        int index = Integer.parseInt(String.valueOf(sIndex)) - 1;
        if (isEnumType()) {
            String repleacedType = type.replace("'", "");
            return repleacedType.substring(5, repleacedType.length() - 1).split(",")[index];
        } else {
            return index;
        }
    }
}
