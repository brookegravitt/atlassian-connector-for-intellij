package com.atlassian.theplugin.bamboo;

import junit.framework.TestCase;

import java.util.Collection;

import com.atlassian.theplugin.configuration.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-16
 * Time: 11:48:22
 * To change this template use File | Settings | File Templates.
 */
public class BambooServerFacadeTest extends TestCase {
    protected void setUp() throws Exception {
        super.setUp();

        BambooConfigurationBean configuration = new BambooConfigurationBean();
        ServerBean server = new ServerBean();
        server.setName("TestServer");
        server.setUrlString("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/");
        server.setUsername("user");
        server.setPassword("user");
        configuration.setServerData(server);

        PluginConfigurationBean pluginConfig = new PluginConfigurationBean();
        pluginConfig.setBambooConfigurationData(configuration);

        ConfigurationFactory.setConfiguration(pluginConfig);
    }

    public void testSubscribedBuildStatus() throws Exception {
        BambooServerFactory factory = new BambooServerFactory();
        BambooServerFacade facade = factory.getBambooServerFacade();
        Collection<BambooBuild> plans = facade.getSubscribedPlansResults();

        assertNotNull(plans);
        assertFalse(plans.size() == 0);
    }
}
