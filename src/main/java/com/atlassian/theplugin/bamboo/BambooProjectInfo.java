package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.bamboo.BambooProject;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-16
 * Time: 09:11:54
 * To change this template use File | Settings | File Templates.
 */
public class BambooProjectInfo implements BambooProject {
    private String name;
    private String key;

    public BambooProjectInfo(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public String getProjectName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
