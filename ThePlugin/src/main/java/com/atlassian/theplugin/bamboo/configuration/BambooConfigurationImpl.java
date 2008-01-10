package com.atlassian.theplugin.bamboo.configuration;

/**
 * Initial (dummy) implementation of bamboo config
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 12:00:32 PM
 */
class BambooConfigurationImpl implements Configuration {
    ServerImpl server = new ServerImpl();

    public Server getServer() {
        return server;
    }

    public void setServer(Server newConfiguration) {
        server = new ServerImpl(newConfiguration);
    }
}
