package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.idea.PluginInfo;
import junit.framework.TestCase;

public class ConfigurationTest extends TestCase {
	protected void setUp() throws Exception {
		ConfigurationFactory.setConfiguration(new PluginConfigurationBean());
	}

	public void testConfiguration() throws ServerPasswordNotProvidedException {
		BambooConfiguration cfg = ConfigurationFactory.getConfiguration().getBambooConfiguration();
		assertNotNull(cfg);

		Server srv1 = cfg.getServer();
		assertNotNull(srv1);

		ServerBean srv = new ServerBean();
		srv.setName("dummyName");
		srv.setUrlString("http://dummy.url");
		srv.setUsername("dummyUserName<a>aa</a>");
		srv.setPasswordString("dummyPassword", true);

		((BambooConfigurationBean) cfg).setServerData(srv);


		Server server = cfg.getServer();
		assertNotSame(srv1, server);

		assertEquals("dummyName", server.getName());
		assertEquals("http://dummy.url", server.getUrlString());
		assertEquals("dummyUserName<a>aa</a>", server.getUsername());
		assertEquals("dummyPassword", server.getPasswordString());


	}


    public void testProjectSettings() {
        assertEquals("The Plugin", PluginInfo.getName());
    }
}
