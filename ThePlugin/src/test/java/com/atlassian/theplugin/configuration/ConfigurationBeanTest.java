package com.atlassian.theplugin.configuration;

import junit.framework.TestCase;

public class ConfigurationBeanTest extends TestCase
{
    public void testBean()
    {
        ProductServerConfiguration config = new BambooConfigurationBean();

        assertTrue(config.getEnabledServers().isEmpty());

        // try a single server
        Server server = new ServerBean();
        config.storeServer(server);
        assertEquals(server, config.getServer(server));

        // now try another server
        Server server2 = new ServerBean();
        config.storeServer(server2);
        assertEquals(2, config.getServers().size());
        assertTrue(config.getServers().contains(server));
        assertTrue(config.getServers().contains(server2));

        // now disable and try to retrieve
        ServerBean disabledServer = new ServerBean(server2);
        disabledServer.setEnabled(false);
        config.storeServer(disabledServer);
        assertEquals(1, config.getEnabledServers().size());
        assertTrue(config.getEnabledServers().contains(server));
        assertTrue(!config.getEnabledServers().contains(server2));
        assertTrue(!config.getEnabledServers().contains(disabledServer));

        // now try to remove
        config.removeServer(server);
        assertEquals(1, config.getServers().size());
        assertTrue(config.getServers().contains(server2));
        
        // now check our persisted data is right
        assertEquals(config.getServers(), ((BambooConfigurationBean)config).getServersData());
    }
}
