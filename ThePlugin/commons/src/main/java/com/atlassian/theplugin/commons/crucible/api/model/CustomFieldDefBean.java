package com.atlassian.theplugin.commons.crucible.api.model;

import java.util.List;
import java.util.ArrayList;

public class CustomFieldDefBean implements CustomFieldDef {
    private CustomFieldValueType type;
    private int configVersion;
    private String fieldScope;
    private String name;
    private String label;
    private CustomFieldValue defaultValue;
    private List<CustomFieldValue> values;

    public CustomFieldDefBean() {
        values = new ArrayList<CustomFieldValue>();
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public CustomFieldValue getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(CustomFieldValue defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<CustomFieldValue> getValues() {
        return values;
    }

    public void setValues(List<CustomFieldValue> values) {
        this.values = values;
    }
}