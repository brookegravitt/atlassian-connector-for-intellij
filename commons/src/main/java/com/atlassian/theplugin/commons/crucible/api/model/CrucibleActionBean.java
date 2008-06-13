package com.atlassian.theplugin.commons.crucible.api.model;

public class CrucibleActionBean implements CrucibleAction {
    private String name;
    private String displayName;

    public CrucibleActionBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
