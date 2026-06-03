package com.ego.restaurant.models;

public class OrderDetail {
    private String detailId, orderId, itemId, itemName, imageUrl;
    private int quantity;
    private double unitPrice;
    private String note, tableId, tableName, status;
    private long orderTime, finishedAt;

    public OrderDetail() {
    }

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String v) {
        detailId = v;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String v) {
        orderId = v;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String v) {
        itemId = v;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String v) {
        itemName = v;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String v) {
        imageUrl = v;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int v) {
        quantity = v;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double v) {
        unitPrice = v;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String v) {
        note = v;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String v) {
        tableId = v;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String v) {
        tableName = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public long getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(long v) {
        orderTime = v;
    }

    public long getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(long v) {
        finishedAt = v;
    }

    public double getSubTotal() {
        return unitPrice * quantity;
    }
}
