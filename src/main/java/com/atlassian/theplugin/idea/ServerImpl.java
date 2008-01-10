package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.configuration.Server;

/**
 * Configuration for a single Bamboo server.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 11:51:08 AM
 */
class ServerImpl implements Server {
    private String name;
    private String urlString;
    private String username;
    private String password;

    ServerImpl(Server s) {
        name = s.getName();
        urlString = s.getUrlString();
        username = s.getUsername();
        password = s.getPassword();
    }

    ServerImpl() {
    }

    public String getName() {
        return name;
    }

    ServerImpl setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrlString() {
        return urlString;
    }

    ServerImpl setUrlString(String urlString) {
        this.urlString = urlString;
        return this;
    }

    public String getUsername() {
        return username;
    }

    ServerImpl setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    ServerImpl setPassword(String password) {
        this.password = password;
        return this;
    }
}