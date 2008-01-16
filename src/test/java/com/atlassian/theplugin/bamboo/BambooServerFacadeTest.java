package com.atlassian.theplugin.bamboo;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.ArrayList;

import com.atlassian.theplugin.configuration.*;
import com.atlassian.theplugin.api.bamboo.BambooLoginException;

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

        ArrayList<SubscribedPlanBean> plans = new ArrayList<SubscribedPlanBean>();
        SubscribedPlanBean plan = new SubscribedPlanBean();
        plan.setPlanId("TP-DEF");
        plan.setServerData(server);
        plans.add(plan);
                
        configuration.setSubscribedPlansData(plans);
        PluginConfigurationBean pluginConfig = new PluginConfigurationBean();
        pluginConfig.setBambooConfigurationData(configuration);

        ConfigurationFactory.setConfiguration(pluginConfig);
    }

    public void testSubscribedBuildStatus() throws Exception {

        
        Collection<BambooBuild> plans =  BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults();

        assertNotNull(plans);
        assertFalse(plans.size() == 0);
    }

    public void testConnectionTest(){
        BambooServerFacade facade = BambooServerFactory.getBambooServerFacade();
        Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer();
        try {
            facade.testServerConnection(server.getUrlString(), server.getUsername(), server.getPassword());
        } catch (BambooLoginException e) {
            fail();
        }

        try {
            facade.testServerConnection("", "", "");
            fail();
        } catch (BambooLoginException e) {

        }

        try {
            facade.testServerConnection(server.getUrlString(), "", "");
            fail();
        } catch (BambooLoginException e) {

        }

        try {
            facade.testServerConnection("", server.getUsername(), "");
            fail();
        } catch (BambooLoginException e) {

        }

        try {
            facade.testServerConnection("", "", server.getPassword());
            fail();
        } catch (BambooLoginException e) {

        }

    }
}
