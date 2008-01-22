package com.atlassian.theplugin.configuration;

import junit.framework.TestCase;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.BambooConfiguration;
import com.atlassian.theplugin.configuration.ConfigurationFactory;

public class ConfigurationTest extends TestCase {
    protected void setUp() throws Exception {
        ConfigurationFactory.setConfiguration(new PluginConfigurationBean()) ;
    }

    public void testConfiguration() {
        BambooConfiguration cfg = ConfigurationFactory.getConfiguration().getBambooConfiguration();
        assertNotNull(cfg);

        Server srv1 = cfg.getServer();
        assertNotNull(srv1);

        ServerBean srv = new ServerBean();
        srv.setName("dummyName");
        srv.setUrlString("http://dummy.url");
        srv.setUsername("dummyUserName<a>aa</a>");
        srv.setPassword("dummyPassword");

        ((BambooConfigurationBean)cfg).setServerData(srv);


        Server server = cfg.getServer();
        assertNotSame(srv1, server);

        assertEquals("dummyName", server.getName());
        assertEquals("http://dummy.url", server.getUrlString());
        assertEquals("dummyUserName<a>aa</a>", server.getUsername());
        assertEquals("dummyPassword", server.getPassword());


    }
}
