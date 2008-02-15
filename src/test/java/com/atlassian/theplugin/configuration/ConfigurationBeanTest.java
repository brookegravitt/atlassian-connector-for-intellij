package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.XmlSerializer;
import junit.framework.TestCase;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

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

    public void testPersistence() throws Exception
    {
        // first, test a single ServerBean
        ServerBean server = new ServerBean();
        server.setUrlString("http://www.poland.pl");
        server.setUserName("sopot");
        server.setPasswordString("gdansk", true);
        Element e = XmlSerializer.serialize(server);
        assertEquals(server, XmlSerializer.deserialize(e, ServerBean.class));

        // now add a plan to the server and try again
        List plans = new ArrayList();
        SubscribedPlanBean plan = new SubscribedPlanBean("FOO-TESTS");
        plans.add(plan);
        server.setSubscribedPlansData(plans);
        e = XmlSerializer.serialize(server);
        assertEquals(server, XmlSerializer.deserialize(e, ServerBean.class));

        // now add the ServerBean to a BambooConfigurationBean
        BambooConfigurationBean bambooConfig = new BambooConfigurationBean();
        bambooConfig.storeServer(server);
		e = XmlSerializer.serialize(bambooConfig);
        assertEquals(bambooConfig, XmlSerializer.deserialize(e, BambooConfigurationBean.class));

		List<SubscribedPlanBean> plans2 = new ArrayList<SubscribedPlanBean>(plans);
		ServerBean server2 = new ServerBean(server);
		plans2.add(new SubscribedPlanBean("FOO-TEST2"));
		server2.setSubscribedPlansData(plans2);
		bambooConfig.storeServer(server2);
		Collection<Server> servers = bambooConfig.getServers();
		assertEquals(server2.getSubscribedPlans().size(), servers.iterator().next().getSubscribedPlans().size());
		// now roll that up into the global configuration
        PluginConfigurationBean config = new PluginConfigurationBean();
        config.setBambooConfigurationData(bambooConfig);
        e = XmlSerializer.serialize(config);
        assertEquals(config, XmlSerializer.deserialize(e, PluginConfigurationBean.class));
    }
}
