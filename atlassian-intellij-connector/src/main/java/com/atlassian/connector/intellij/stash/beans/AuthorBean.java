package com.atlassian.connector.intellij.stash.beans;

import com.atlassian.connector.intellij.stash.Author;

public class AuthorBean implements Author{
    private UserBean user;

    public String getName() {
        return user.getDisplayName();
    }

    public void setName(String name) {
    }


}
