package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.configuration.Configuration;
import com.atlassian.theplugin.bamboo.configuration.Server;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 4:13:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooConfiguration implements Configuration {
    private Server server = new ServerImpl();

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public String getServerName() {
        return server.getName();
    }

    public void setServerName(final String serverName) {
        server = new ServerImpl(server).setName(serverName);
    }

    public String getServerUrl() {
        return server.getUrlString();
    }

    public void setServerUrl(final String serverUrl) {
        server = new ServerImpl(server).setUrlString(serverUrl);
    }

    public String getUsername() {
        return server.getUsername();
    }

    public void setUsername(final String username) {
        server = new ServerImpl(server).setUsername(username);
    }

    public String getPassword() {
        return server.getPassword();
    }

    public void setPassword(final String password) {
        server = new ServerImpl(server).setPassword(password);
    }
}
