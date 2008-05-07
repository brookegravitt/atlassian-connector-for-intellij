/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.commons.*;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.configuration.*;
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
		assertFalse(cfg.transientGetServers().iterator().hasNext());

        ServerBean srv = new ServerBean();
        srv.setName("dummyName");
        srv.setUrlString("http://dummy.url");
        srv.setUserName("dummyUserName<a>aa</a>");
        srv.transientSetPasswordString("dummyPassword", true);

		serversData.add(srv);

		((BambooConfigurationBean) cfg).setServersData(serversData);

        Server server = cfg.transientGetServers().iterator().next();
		assertNotNull(server);

        assertEquals("dummyName", server.getName());
        assertEquals("http://dummy.url", server.getUrlString());
        assertEquals("dummyUserName<a>aa</a>", server.getUserName());
        assertEquals("dummyPassword", server.transientGetPasswordString());

        // now let's test cloning a configuration
        PluginConfigurationBean newConfig = new PluginConfigurationBean(ConfigurationFactory.getConfiguration());
        assertEquals(ConfigurationFactory.getConfiguration(), newConfig);
    }

	public void testServerBeanClone() throws Exception {
		Server srv = new ServerBean();
		srv.setName("dummyName");
		srv.setUrlString("http://dummy.url");
		srv.setUserName("dummyUserName<a>aa</a>");
		srv.transientSetPasswordString("dummyPassword", true);

		List<SubscribedPlan> plans = new ArrayList<SubscribedPlan>();
		SubscribedPlanBean plan = new SubscribedPlanBean();
		plan.setPlanId("dummyPlan");
		plans.add(plan);
		srv.transientSetSubscribedPlans(plans);

		Server cloned = new ServerBean(srv);

		assertNotSame(srv, cloned);
		assertEquals(srv, cloned);
		assertTrue(srv.equals(cloned));
		assertTrue(srv.equals(srv));
		assertFalse(srv.equals(null));

		ServerBean clonedBean2 = new ServerBean(srv);
		clonedBean2.setUid(0);
		assertFalse(srv.equals(clonedBean2));
		clonedBean2.setUid(srv.getUid());
		assertTrue(srv.equals(clonedBean2));

		clonedBean2 = new ServerBean(srv);
		clonedBean2.setUserName("");
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
		clonedBean2.transientSetPasswordString("", true);
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = new ServerBean(srv);
		clonedBean2.transientGetSubscribedPlans().clear();
		assertFalse(srv.equals(clonedBean2));

		clonedBean2 = new ServerBean(srv);
		clonedBean2.setShouldPasswordBeStored(!srv.getShouldPasswordBeStored());
		assertFalse(srv.equals(clonedBean2));
	}


	public void testProjectSettings() {
        assertEquals("Atlassian", PluginUtil.getInstance().getName());
    }
}
