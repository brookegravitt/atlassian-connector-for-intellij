package com.atlassian.theplugin.bamboo.configuration;

import junit.framework.TestCase;

public class ConfigurationTest extends TestCase {
    protected void setUp() throws Exception {
        ConfigurationFactory.setConfiguration(new BambooConfigurationImpl()) ;
    }

    public void testConfiguration() {
        Configuration cfg = ConfigurationFactory.getConfiguration();
        assertNotNull(cfg);

        Server srv1 = cfg.getServer();
        assertNotNull(srv1);

        BambooConfigurationImpl.ServerImpl srv = new BambooConfigurationImpl.ServerImpl();
        srv.setName("dummyName");
        srv.setUrlString("http://dummy.url");
        srv.setUsername("dummyUserName");
        srv.setPassword("dummyPassword");

        ((BambooConfigurationImpl)cfg).setServer(srv);


        Server server = cfg.getServer();
        assertNotSame(srv, server); /* config should create a copy */
        assertNotSame(srv1, server);

        assertEquals("dummyName", server.getName());
        assertEquals("http://dummy.url", server.getUrlString());
        assertEquals("dummyUserName", server.getUsername());
        assertEquals("dummyPassword", server.getPassword());


    }
}
