package com.fw.edc;

import java.util.List;

public class ChangeTable {
    private static int FIELD_NUM = 20;
    private String type;
    private String database;
    private String table;
    private List<List<PackagedEvent.Field>> recordFields;

    public ChangeTable(PackagedEvent pe) {
        this.type = PackagedEvent.showType(pe.getType());
        this.database = pe.getDatabase();
        this.table = pe.getTableName();
        this.recordFields = pe.getRecordFields();
    }

    private String generateHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.type)
                .append("   ")
                .append(this.database)
                .append(".")
                .append(this.table)
                .append("\n┌");
        for (int i = 0; i < getTotalNum(); i++) {
            sb.append("─");
        }
        sb.append("┐");

        return sb.toString();
    }

    private String generateBody() {
        return null;
    }

    private String generateFooter() {
        return null;
    }

    public void refresh() {

    }

    public String output() {
        return generateHeader();
    }

    private int getValueNum() {
        return (getTotalNum() - FIELD_NUM) / 2;
    }

    private int getTotalNum() {
        return Util.getScreenWidth() * 2 / 15;
    }

}