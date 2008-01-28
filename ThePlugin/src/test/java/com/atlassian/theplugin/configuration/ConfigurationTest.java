package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.idea.PluginInfo;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.ArrayList;

public class ConfigurationTest extends TestCase {
    protected void setUp() throws Exception {
        ConfigurationFactory.setConfiguration(new PluginConfigurationBean()) ;
    }

    public void testConfiguration() throws ServerPasswordNotProvidedException {
        BambooConfiguration cfg = ConfigurationFactory.getConfiguration().getBambooConfiguration();

		Collection<ServerBean> serversData = new ArrayList<ServerBean>();

		assertNotNull(cfg);
		assertFalse(cfg.getServers().iterator().hasNext());


        ServerBean srv = new ServerBean();
        srv.setName("dummyName");
        srv.setUrlString("http://dummy.url");
        srv.setUsername("dummyUserName<a>aa</a>");
        srv.setPasswordString("dummyPassword", true);

		serversData.add(srv);

		((BambooConfigurationBean)cfg).setServersData(serversData);


        Server server = cfg.getServers().iterator().next();
		assertNotNull(server);

        assertEquals("dummyName", server.getName());
        assertEquals("http://dummy.url", server.getUrlString());
        assertEquals("dummyUserName<a>aa</a>", server.getUsername());
        assertEquals("dummyPassword", server.getPasswordString());


	}


    public void testProjectSettings() {
        assertEquals("The Plugin", PluginInfo.getName());
    }
}
