package com.atlassian.connector.intellij.stash.beans;

import com.atlassian.connector.intellij.stash.Author;

public class AuthorBean implements Author{
    private UserBean user;

    public String getName() {
        return user.displayName;
    }

    public void setName(String name) {
    }


    private static class UserBean
    {
        private String displayName;
        private String name;
    }
}
