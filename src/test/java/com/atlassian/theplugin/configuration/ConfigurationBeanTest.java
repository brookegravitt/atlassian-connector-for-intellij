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

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.SubscribedPlan;
import com.atlassian.theplugin.commons.configuration.*;
import com.intellij.util.xmlb.XmlSerializer;
import junit.framework.TestCase;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConfigurationBeanTest extends TestCase {
    public void testBean() {
        ProductServerConfiguration config = new BambooConfigurationBean();

        assertTrue(config.transientgetEnabledServers().isEmpty());

        // try a single server
        Server server = new ServerBean();
        config.storeServer(server);
        assertEquals(server, config.transientGetServer(server));

        // now try another server
        Server server2 = new ServerBean();
        config.storeServer(server2);
        assertEquals(2, config.transientGetServers().size());
        assertTrue(config.transientGetServers().contains(server));
        assertTrue(config.transientGetServers().contains(server2));

        // now disable and try to retrieve
        Server disabledServer = new ServerBean(server2);
        disabledServer.setEnabled(false);
        config.storeServer(disabledServer);
        assertEquals(1, config.transientgetEnabledServers().size());
        assertTrue(config.transientgetEnabledServers().contains(server));
        assertTrue(!config.transientgetEnabledServers().contains(server2));
        assertTrue(!config.transientgetEnabledServers().contains(disabledServer));

        // now try to remove
        config.removeServer(server);
        assertEquals(1, config.transientGetServers().size());
        assertTrue(config.transientGetServers().contains(server2));

        // now check our persisted data is right
        assertEquals(config.transientGetServers(), ((BambooConfigurationBean) config).getServersData());
    }

    public void testPersistence() throws Exception {
        // first, test a single ServerBean
        Server server = new ServerBean();
        server.setUrlString("http://www.poland.pl");
        server.setUserName("sopot");
        server.transientSetPasswordString("gdansk", true);
        Element e = XmlSerializer.serialize(server);
        assertEquals(server, XmlSerializer.deserialize(e, ServerBean.class));

        // now add a plan to the server and try again
        List<SubscribedPlan> plans = new ArrayList<SubscribedPlan>();
        SubscribedPlanBean plan = new SubscribedPlanBean("FOO-TESTS");
        plans.add(plan);
        server.transientSetSubscribedPlans(plans);
        e = XmlSerializer.serialize(server);
        assertEquals(server, XmlSerializer.deserialize(e, ServerBean.class));

        // now add the ServerBean to a BambooConfigurationBean
        BambooConfigurationBean bambooConfig = new BambooConfigurationBean();
        bambooConfig.storeServer(server);
        e = XmlSerializer.serialize(bambooConfig);
        assertEquals(bambooConfig, XmlSerializer.deserialize(e, BambooConfigurationBean.class));

        List<SubscribedPlan> plans2 = new ArrayList<SubscribedPlan>(plans);
        Server server2 = new ServerBean(server);
        plans2.add(new SubscribedPlanBean("FOO-TEST2"));
        server2.transientSetSubscribedPlans(plans2);
        bambooConfig.storeServer(server2);
        Collection<Server> servers = bambooConfig.transientGetServers();
        assertEquals(server2.transientGetSubscribedPlans().size(), servers.iterator().next().transientGetSubscribedPlans().size());
        // now roll that up into the global configuration
        PluginConfigurationBean config = new PluginConfigurationBean();
        config.setBambooConfigurationData(bambooConfig);
        e = XmlSerializer.serialize(config);
        assertEquals(config, XmlSerializer.deserialize(e, PluginConfigurationBean.class));
    }
}
