package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.configuration.Configuration;
import com.intellij.util.xmlb.annotations.Transient;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 4:13:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooConfiguration implements Configuration {
    private ServerImpl server = new ServerImpl();

    public ServerImpl getServer() {
        return server;
    }

    public void setServer(ServerImpl server) {
        this.server = server;
    }

    @Transient
    public String getServerName() {
        return server.getName();
    }

    public void setServerName(final String serverName) {
        server = new ServerImpl(server);
        server.setName(serverName);
    }

    @Transient
    public String getServerUrl() {
        return server.getUrlString();
    }

    public void setServerUrl(final String serverUrl) {
        server = new ServerImpl(server);
        server.setUrlString(serverUrl);
    }

    @Transient
    public String getUsername() {
        return server.getUsername();
    }

    public void setUsername(final String username) {
        server = new ServerImpl(server);
        server.setUsername(username);
    }

    @Transient
    public String getPassword() {
        return server.getPassword();
    }

    public void setPassword(final String password) {
        server = new ServerImpl(server);
        server.setPassword(password);
    }
}
