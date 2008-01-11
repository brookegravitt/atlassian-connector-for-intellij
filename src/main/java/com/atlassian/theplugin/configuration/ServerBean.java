package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.configuration.Server;

/**
 * BambooConfigurationBean for a single Bamboo server.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 11:51:08 AM
 */
public class ServerBean implements Server {
    private String name;
    private String urlString;
    private String username;
    private String password;

    ServerBean(Server s) {
        name = s.getName();
        urlString = s.getUrlString();
        username = s.getUsername();
        password = s.getPassword();
    }

    public ServerBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}