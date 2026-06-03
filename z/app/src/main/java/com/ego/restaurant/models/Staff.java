package com.ego.restaurant.models;

public class Staff {
    private int id, status;
    private String name, role;

    public Staff() {
    }

    public int getId() {
        return id;
    }

    public void setId(int v) {
        id = v;
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String v) {
        role = v;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int v) {
        status = v;
    }
}
