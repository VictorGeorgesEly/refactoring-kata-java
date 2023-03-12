package com.sipios.refactoring.entity;

public class Body {

    private Item[] items;
    private String type;

    public Body(Item[] items, String type) {
        this.items = items;
        this.type = type;
    }

    public Item[] getItems() {
        return items;
    }

    public void setItems(Item[] items) {
        this.items = items;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
