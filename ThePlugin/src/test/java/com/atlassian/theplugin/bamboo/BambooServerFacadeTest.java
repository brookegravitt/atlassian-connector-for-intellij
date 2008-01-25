package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.configuration.*;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-16
 * Time: 11:48:22
 * To change this template use File | Settings | File Templates.
 */
public class BambooServerFacadeTest extends TestCase {
    private PluginConfigurationBean pluginConfig;
    private PluginConfigurationBean badLoginPluginConfig;
    private PluginConfigurationBean badPlanPluginConfig;
    private ServerBean server;

    public BambooServerFacadeTest() {
        BambooConfigurationBean configuration = new BambooConfigurationBean();
        server = new ServerBean();
        server.setName("TestServer");
        server.setUrlString("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/");
        server.setUsername("user");
        server.setPasswordString("d0n0tch@nge", true);

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
        badServer.setName(server.getUrlString());
        badServer.setUrlString(server.getUrlString());
        badServer.setUsername(server.getUsername());
        badServer.setPasswordString("xxx", true);

        ArrayList<SubscribedPlanBean> badPlans = new ArrayList<SubscribedPlanBean>();
        SubscribedPlanBean badPlan = new SubscribedPlanBean();
        badPlan.setPlanId("TP-DEF-BAD");
        badPlans.add(badPlan);
        badServer.setSubscribedPlansData(badPlans);

        badConfiguration.setServerData(badServer);

        BambooConfigurationBean badPlanConfiguration = new BambooConfigurationBean();
        ServerBean badPlanServer = new ServerBean();
        badPlanServer.setName(server.getName());
        badPlanServer.setUrlString(server.getUrlString());
        badPlanServer.setUsername(server.getUsername());
        badPlanServer.setPasswordString(server.getEncryptedPassword(), true);
        badPlanServer.setSubscribedPlansData(badPlans);

        badPlanConfiguration.setServerData(badPlanServer);

        badLoginPluginConfig = new PluginConfigurationBean();
        badLoginPluginConfig.setBambooConfigurationData(badConfiguration);

        badPlanPluginConfig = new PluginConfigurationBean();
        badPlanPluginConfig.setBambooConfigurationData(badPlanConfiguration);

    }

    protected void setUp() throws Exception {
        super.setUp();

        ConfigurationFactory.setConfiguration(pluginConfig);
    }

    public void testSubscribedBuildStatus() throws Exception {
        Collection<BambooBuild> plans = BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults();
        assertNotNull(plans);
        assertFalse(plans.size() == 0);
    }

    public void testFailedLoginSubscribedBuildStatus() throws Exception {
        ConfigurationFactory.setConfiguration(badLoginPluginConfig);
        Collection<BambooBuild> plans = BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults();
        assertNotNull(plans);
        assertEquals(1, plans.size());
        BambooBuild build = plans.iterator().next();
        assertEquals(BuildStatus.ERROR, build.getStatus());
        assertEquals("TP-DEF-BAD", build.getBuildKey());
        assertEquals("The user does not have sufficient permissions to perform this action.\n", build.getMessage());
    }

    public void testBadPlansSubscribedBuildStatus() throws Exception {
        ConfigurationFactory.setConfiguration(badPlanPluginConfig);
        Collection<BambooBuild> plans = BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults();
        assertNotNull(plans);
        assertEquals(1, plans.size());
        BambooBuild build = plans.iterator().next();
        assertEquals(BuildStatus.ERROR, build.getStatus());
        assertEquals("TP-DEF-BAD", build.getBuildKey());
        assertEquals("The user does not have sufficient permissions to perform this action.\n", build.getMessage());
    }

    public void testProjectList() throws Exception {
        Collection<BambooProject> projects = BambooServerFactory.getBambooServerFacade().getProjectList();
        assertNotNull(projects);
        assertFalse(projects.size() == 0);
    }

    public void testFailedProjectList() throws Exception {
        ConfigurationFactory.setConfiguration(badLoginPluginConfig);
        Collection<BambooProject> projects = BambooServerFactory.getBambooServerFacade().getProjectList();
        assertNull(projects);
    }

    public void testPlanList() throws Exception {
        Collection<BambooPlan> plans = BambooServerFactory.getBambooServerFacade().getPlanList();
        assertNotNull(plans);
        assertFalse(plans.size() == 0);
    }

    public void testFailedPlanList() throws Exception {
        ConfigurationFactory.setConfiguration(badLoginPluginConfig);
        Collection<BambooPlan> plans = BambooServerFactory.getBambooServerFacade().getPlanList();
        assertNull(plans);
    }

    public void testConnectionTest() throws ServerPasswordNotProvidedException {
        BambooServerFacade facade = BambooServerFactory.getBambooServerFacade();
        Server aServer = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServer();
        try {
            facade.testServerConnection(aServer.getUrlString(), aServer.getUsername(), aServer.getPasswordString());
        } catch (BambooLoginException e) {
            fail();
        }

        try {
            facade.testServerConnection("", "", "");
            fail();
        } catch (BambooLoginException e) {

        }

        try {
            facade.testServerConnection(aServer.getUrlString(), "", "");
            fail();
        } catch (BambooLoginException e) {

        }

        try {
            facade.testServerConnection("", aServer.getUsername(), "");
            fail();
        } catch (BambooLoginException e) {

        }

        try {
            facade.testServerConnection("", "", aServer.getPasswordString());
            fail();
        } catch (BambooLoginException e) {

        }

    }


    public void testBambooConnectionWithEmptyPlan()
            throws BambooLoginException, CloneNotSupportedException, ServerPasswordNotProvidedException {
        server.setSubscribedPlansData(new ArrayList<SubscribedPlanBean>());
        BambooServerFacade facade = new BambooServerFacadeImpl();
        Collection<BambooBuild> plans = facade.getSubscribedPlansResults();
        assertEquals(0, plans.size());
    }
}
