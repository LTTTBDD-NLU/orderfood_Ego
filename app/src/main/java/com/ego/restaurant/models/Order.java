package com.ego.restaurant.models;

import java.util.ArrayList;

public class Order {
    private String orderId, tableId, tableName, userId, roleCode, orderStatus;
    private double totalAmount;
    private long createdAt, paidAt;
    private ArrayList<OrderDetail> items;

    public Order() {
        items = new ArrayList<>();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String v) {
        orderId = v;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String v) {
        userId = v;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String v) {
        roleCode = v;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String v) {
        orderStatus = v;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double v) {
        totalAmount = v;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long v) {
        createdAt = v;
    }

    public long getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(long v) {
        paidAt = v;
    }

    public ArrayList<OrderDetail> getItems() {
        return items;
    }

    public void setItems(ArrayList<OrderDetail> v) {
        items = v != null ? v : new ArrayList<>();
    }
}
