package com.atlassian.theplugin.bamboo;

import junit.framework.TestCase;

import java.util.Collection;
import java.util.ArrayList;

import com.atlassian.theplugin.configuration.*;
import com.atlassian.theplugin.bamboo.api.BambooLoginException;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-16
 * Time: 11:48:22
 * To change this template use File | Settings | File Templates.
 */
public class BambooServerFacadeTest extends TestCase {
    private PluginConfigurationBean pluginConfig;
    private PluginConfigurationBean badPluginConfig;

    public BambooServerFacadeTest() {
        BambooConfigurationBean configuration = new BambooConfigurationBean();
        ServerBean server = new ServerBean();
        server.setName("TestServer");
        server.setUrlString("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/");
        server.setUsername("user");
        server.setPassword("d0n0tch@nge");

        ArrayList<SubscribedPlanBean> plans = new ArrayList<SubscribedPlanBean>();
        SubscribedPlanBean plan = new SubscribedPlanBean();
        plan.setPlanId("TP-DEF");
        plans.add(plan);
        server.setSubscribedPlansData(plans);

        configuration.setServerData(server);
        pluginConfig = new PluginConfigurationBean();
        pluginConfig.setBambooConfigurationData(configuration);

        BambooConfigurationBean badConfiguration = new BambooConfigurationBean();
        ServerBean badServer = new ServerBean();
        badServer.setName("TestServer");
        badServer.setUrlString("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/");
        badServer.setUsername("user");
        badServer.setPassword("xxx");

        ArrayList<SubscribedPlanBean> badPlans = new ArrayList<SubscribedPlanBean>();
        SubscribedPlanBean badPlan = new SubscribedPlanBean();
        badPlan.setPlanId("TP-DEF-BAD");
        badPlans.add(badPlan);
        badServer.setSubscribedPlansData(badPlans);

        badConfiguration.setServerData(badServer);

        badPluginConfig = new PluginConfigurationBean();
        badPluginConfig.setBambooConfigurationData(badConfiguration);
    }

    protected void setUp() throws Exception {
        super.setUp();

        ConfigurationFactory.setConfiguration(pluginConfig);
    }

    public void testSubscribedBuildStatus() throws Exception {
        Collection<BambooBuild> plans =  BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults();
        assertNotNull(plans);
        assertFalse(plans.size() == 0);
    }

    public void testFailedSubscribedBuildStatus() throws Exception {
        ConfigurationFactory.setConfiguration(badPluginConfig);
        Collection<BambooBuild> plans =  BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults();
        assertNotNull(plans);
        assertEquals(1, plans.size());
        BambooBuild build = plans.iterator().next();
        assertEquals(BuildStatus.ERROR, build.getStatus());
        assertEquals("TP-DEF-BAD", build.getBuildKey());
        assertEquals("The user does not have sufficient permissions to perform this action.\n", build.getMessage());
    }

    public void testProjectList() throws Exception {
        Collection<BambooProject> projects =  BambooServerFactory.getBambooServerFacade().getProjectList();
        assertNotNull(projects);
        assertFalse(projects.size() == 0);
    }

    public void testFailedProjectList() throws Exception {
        ConfigurationFactory.setConfiguration(badPluginConfig);        
        Collection<BambooProject> projects =  BambooServerFactory.getBambooServerFacade().getProjectList();
        assertNull(projects);
    }

    public void testPlanList() throws Exception {
        Collection<BambooPlan> plans =  BambooServerFactory.getBambooServerFacade().getPlanList();
        assertNotNull(plans);
        assertFalse(plans.size() == 0);
    }

    public void testFailedPlanList() throws Exception {
        ConfigurationFactory.setConfiguration(badPluginConfig);        
        Collection<BambooPlan> plans =  BambooServerFactory.getBambooServerFacade().getPlanList();
        assertNull(plans);
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
