package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.crucible.api.UserData;

public class UserListItem {
    private UserData user;
    private boolean selected;

    public UserListItem(UserData user, boolean selected) {
        this.user = user;
        this.selected = selected;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
