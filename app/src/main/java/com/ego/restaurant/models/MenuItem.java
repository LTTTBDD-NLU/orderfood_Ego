package com.ego.restaurant.models;

import java.io.Serializable;

public class MenuItem implements Serializable {
    private String itemId, itemName, categoryId, imageUrl, status, note;
    private double guestPrice, memberPrice;
    private int quantity;

    public MenuItem() {
    }

    public MenuItem(String itemId, String itemName, String imageUrl,
                    double guestPrice, double memberPrice, String status) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.imageUrl = imageUrl;
        this.guestPrice = guestPrice;
        this.memberPrice = memberPrice;
        this.status = status;
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

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String v) {
        categoryId = v;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String v) {
        imageUrl = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String v) {
        note = v;
    }

    public double getGuestPrice() {
        return guestPrice;
    }

    public void setGuestPrice(double v) {
        guestPrice = v;
    }

    public double getMemberPrice() {
        return memberPrice;
    }

    public void setMemberPrice(double v) {
        memberPrice = v;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int v) {
        quantity = v;
    }

    public double getAppliedPrice(String role) {
        return "MEMBER".equals(role) ? memberPrice : guestPrice;
    }
}
