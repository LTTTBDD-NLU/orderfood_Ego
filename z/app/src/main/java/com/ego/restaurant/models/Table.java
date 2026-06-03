package com.ego.restaurant.models;

public class Table {
    private String tableId;
    private String tableName;
    private String status;

    public Table() {
    }

    public Table(String tableId, String tableName, String status) {
        this.tableId = tableId;
        this.tableName = tableName;
        this.status = status;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String v) {
        this.tableId = v;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String v) {
        this.tableName = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        this.status = v;
    }
}
