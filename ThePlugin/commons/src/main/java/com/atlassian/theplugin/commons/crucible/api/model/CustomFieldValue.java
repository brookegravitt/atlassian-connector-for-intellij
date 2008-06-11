package com.atlassian.theplugin.commons.crucible.api.model;

public class CustomFieldValue {
    private String name;
    private Object value;


    public CustomFieldValue() {
    }

    public CustomFieldValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}