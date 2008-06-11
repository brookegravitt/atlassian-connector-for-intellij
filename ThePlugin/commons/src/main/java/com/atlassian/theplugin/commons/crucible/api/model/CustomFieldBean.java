package com.atlassian.theplugin.commons.crucible.api.model;

public class CustomFieldBean implements CustomField {
    private CustomFieldValueType type;
    private int configVersion;
    private String fieldScope;
    private Object value;
    private String hrValue;

    public CustomFieldValueType getType() {
        return type;
    }

    public void setType(CustomFieldValueType type) {
        this.type = type;
    }

    public int getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(int configVersion) {
        this.configVersion = configVersion;
    }

    public String getFieldScope() {
        return fieldScope;
    }

    public void setFieldScope(String fieldScope) {
        this.fieldScope = fieldScope;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getHrValue() {
        return hrValue;
    }

    public void setHrValue(String hrValue) {
        this.hrValue = hrValue;
    }
}
