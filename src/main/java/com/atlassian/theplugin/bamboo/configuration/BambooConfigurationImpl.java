package com.atlassian.theplugin.bamboo.configuration;

/**
 * Initial (dummy) implementation of bamboo config
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 12:00:32 PM
 */
class BambooConfigurationImpl implements Configuration {
    private Server server = createDummyServer();

    private static Server createDummyServer() {
        ServerImpl s = new ServerImpl();
        s.setName("dummyName");
        s.setUrlString("http://dummy.url");
        s.setUsername("dummyUserName");
        s.setPassword("dummyPassword");
        return s;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server newConfiguration) {
        server = new ServerImpl(newConfiguration);
    }
}
