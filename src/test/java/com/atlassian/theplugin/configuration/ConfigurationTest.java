package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.idea.PluginInfoUtil;
import com.atlassian.theplugin.ServerType;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConfigurationTest extends TestCase {
    protected void setUp() throws Exception {
        ConfigurationFactory.setConfiguration(new PluginConfigurationBean());
    }

    public void testConfiguration() throws ServerPasswordNotProvidedException {
        ProductServerConfiguration cfg = ConfigurationFactory.getConfiguration().getProductServers(ServerType.BAMBOO_SERVER);

		Collection<ServerBean> serversData = new ArrayList<ServerBean>();

		assertNotNull(cfg);
		assertFalse(cfg.getServers().iterator().hasNext());

        ServerBean srv = new ServerBean();
        srv.setName("dummyName");
        srv.setUrlString("http://dummy.url");
        srv.setUsername("dummyUserName<a>aa</a>");
        srv.setPasswordString("dummyPassword", true);

		serversData.add(srv);

		((BambooConfigurationBean) cfg).setServersData(serversData);

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

		ServerBean clonedBean = new ServerBean(srv);

		assertNotSame(srv, clonedBean);
		assertEquals(srv, clonedBean);
		assertTrue(srv.equals(clonedBean));
		assertTrue(srv.equals(srv));
		assertFalse(srv.equals(null));

		ServerBean clonedBean2 = new ServerBean(srv);
		clonedBean2.setUid(0);
		assertFalse(srv.equals(clonedBean2));
		clonedBean2.setUid(srv.getUid());
		assertTrue(srv.equals(clonedBean2));

		clonedBean2 = new ServerBean(srv);
		clonedBean2.setUsername("");
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = new ServerBean(srv);
		clonedBean2.setUrlString("");
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = new ServerBean(srv);
		clonedBean2.setEncryptedPassword("");
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = new ServerBean(srv);
		clonedBean2.setName("");
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = new ServerBean(srv);
		clonedBean2.setPasswordString("", true);
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = new ServerBean(srv);
		clonedBean2.getSubscribedPlansData().clear();
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = new ServerBean(srv);
		clonedBean2.setShouldPasswordBeStored(!srv.getShouldPasswordBeStored());
		assertFalse(srv.equals(clonedBean2));
	}


	public void testProjectSettings() {
        assertEquals("The Plugin", PluginInfoUtil.getName());
    }
}
