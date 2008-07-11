package com.atlassian.theplugin.commons.crucible.api.model;

public class CustomFieldBean implements CustomField {
    private int configVersion;
    private String value;

    public int getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(int configVersion) {
        this.configVersion = configVersion;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
