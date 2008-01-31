package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.idea.PluginInfo;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

	public void testServerBeanClone() throws Exception {
		ServerBean srv = new ServerBean();
		srv.setName("dummyName");
		srv.setUrlString("http://dummy.url");
		srv.setUsername("dummyUserName<a>aa</a>");
		srv.setPasswordString("dummyPassword", true);

		List<SubscribedPlanBean> plans = new ArrayList<SubscribedPlanBean>();
		SubscribedPlanBean plan = new SubscribedPlanBean();
		plan.setPlanId("dummyPlan");
		plans.add(plan);
		srv.setSubscribedPlansData(plans);

		ServerBean clonedBean = (ServerBean) srv.clone();

		assertNotSame(srv, clonedBean);
		assertEquals(srv, clonedBean);
		assertTrue(srv.equals(clonedBean));
		assertTrue(srv.equals(srv));
		assertFalse(srv.equals(null));

		ServerBean clonedBean2 = (ServerBean) srv.clone();
		clonedBean2.setUid(0);
		assertFalse(srv.equals(clonedBean2));
		clonedBean2.setUid(srv.getUid());
		assertTrue(srv.equals(clonedBean2));

		clonedBean2 = (ServerBean) srv.clone();
		clonedBean2.setUsername("");
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = (ServerBean) srv.clone();
		clonedBean2.setUrlString("");
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = (ServerBean) srv.clone();
		clonedBean2.setEncryptedPassword("");
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = (ServerBean) srv.clone();
		clonedBean2.setName("");
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = (ServerBean) srv.clone();
		clonedBean2.setPasswordString("", true);
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = (ServerBean) srv.clone();
		clonedBean2.getSubscribedPlansData().clear();
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = (ServerBean) srv.clone();
		clonedBean2.setShouldPasswordBeStored(!srv.getShouldPasswordBeStored());
		assertFalse(srv.equals(clonedBean2));
	}


	public void testProjectSettings() {
        assertEquals("The Plugin", PluginInfo.getName());
    }
}
