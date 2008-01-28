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
        Collection<ServerBean> servers = new ArrayList<ServerBean>();
		server = new ServerBean();

		server.setName("TestServer");
        server.setUrlString("http://lech.atlassian.pl:8080/atlassian-bamboo-1.2.4/");
        server.setUsername("user");
        server.setPasswordString("d0n0tch@nge", true);
		servers.add(server);

		ArrayList<SubscribedPlanBean> plans = new ArrayList<SubscribedPlanBean>();
        SubscribedPlanBean plan = new SubscribedPlanBean();
        plan.setPlanId("TP-DEF");
        plans.add(plan);
        server.setSubscribedPlansData(plans);

        configuration.setServersData(servers);
        pluginConfig = new PluginConfigurationBean();
        pluginConfig.setBambooConfigurationData(configuration);

        BambooConfigurationBean badConfiguration = new BambooConfigurationBean();
		Collection<ServerBean> badServers = new ArrayList<ServerBean>();
		ServerBean badServer = new ServerBean();
        badServer.setName(server.getUrlString());
        badServer.setUrlString(server.getUrlString());
        badServer.setUsername(server.getUsername());
        badServer.setPasswordString("xxx", true);
		badServers.add(badServer);

		ArrayList<SubscribedPlanBean> badPlans = new ArrayList<SubscribedPlanBean>();
        SubscribedPlanBean badPlan = new SubscribedPlanBean();
        badPlan.setPlanId("TP-DEF-BAD");
        badPlans.add(badPlan);
        badServer.setSubscribedPlansData(badPlans);

        badConfiguration.setServersData(badServers);

        BambooConfigurationBean badPlanConfiguration = new BambooConfigurationBean();
		Collection<ServerBean> badPlanServers = new ArrayList<ServerBean>();
		ServerBean badPlanServer = new ServerBean();
        badPlanServer.setName(server.getName());
        badPlanServer.setUrlString(server.getUrlString());
        badPlanServer.setUsername(server.getUsername());
        badPlanServer.setPasswordString(server.getEncryptedPassword(),true);
        badPlanServer.setSubscribedPlansData(badPlans);

		badPlanServers.add(badPlanServer);
		badPlanConfiguration.setServersData(badPlanServers);

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

		Collection<BambooBuild> plans =  null;
		Collection<Server> servers = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers();

		if (servers.iterator().hasNext()) {
			plans = BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults(servers.iterator().next());
		}

		assertNotNull(plans);
        assertFalse(plans.size() == 0);
    }

    public void testFailedLoginSubscribedBuildStatus() throws Exception {
		Collection<BambooBuild> plans =  null;

		ConfigurationFactory.setConfiguration(badLoginPluginConfig);
		Collection<Server> servers = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers();

		if (servers.iterator().hasNext()) plans = BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults(servers.iterator().next());

		assertNotNull(plans);
        assertEquals(1, plans.size());
        BambooBuild build = plans.iterator().next();
        assertEquals(BuildStatus.ERROR, build.getStatus());
        assertEquals("TP-DEF-BAD", build.getBuildKey());
        assertEquals("Login exception: The user does not have sufficient permissions to perform this action.\n", build.getMessage());
    }

    public void testBadPlansSubscribedBuildStatus() throws Exception {
		Collection<BambooBuild> plans =  null;

		ConfigurationFactory.setConfiguration(badPlanPluginConfig);
		Collection<Server> servers = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers();
		if (servers.iterator().hasNext()) plans = BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults(servers.iterator().next());

		assertNotNull(plans);
        assertEquals(1, plans.size());
        BambooBuild build = plans.iterator().next();
        assertEquals(BuildStatus.ERROR, build.getStatus());
        assertEquals("TP-DEF-BAD", build.getBuildKey());
        assertEquals("The user does not have sufficient permissions to perform this action.\n", build.getMessage());
    }

    public void testProjectList() throws Exception {
		Collection<BambooProject> projects =  null;
		Collection<Server> servers = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers();

		if (servers.iterator().hasNext()) projects =  BambooServerFactory.getBambooServerFacade().getProjectList(servers.iterator().next());
        assertNotNull(projects);
        assertFalse(projects.size() == 0);
    }

    public void testFailedProjectList() throws Exception {
        ConfigurationFactory.setConfiguration(badLoginPluginConfig);
		Collection<BambooProject> projects =  null;
		Collection<Server> servers = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers();

		if (servers.iterator().hasNext()) projects =  BambooServerFactory.getBambooServerFacade().getProjectList(servers.iterator().next());
        assertNull(projects);
    }

    public void testPlanList() throws Exception {
		Collection<BambooPlan> plans =  null;

		ConfigurationFactory.setConfiguration(badPlanPluginConfig);
		Collection<Server> servers = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers();
		if (servers.iterator().hasNext()) plans = BambooServerFactory.getBambooServerFacade().getPlanList(servers.iterator().next());

        assertNotNull(plans);
        assertFalse(plans.size() == 0);
    }

    public void testFailedPlanList() throws Exception {
		Collection<BambooPlan> plans = null;
		ConfigurationFactory.setConfiguration(badLoginPluginConfig);
		Collection<Server> servers = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers();

		if (servers.iterator().hasNext()) {
			plans =  BambooServerFactory.getBambooServerFacade().getPlanList(servers.iterator().next());

		}
        assertNull(plans);
    }

    public void testConnectionTest() throws ServerPasswordNotProvidedException {
        BambooServerFacade facade = BambooServerFactory.getBambooServerFacade();
        Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers().iterator().next();
        try {
            facade.testServerConnection(server.getUrlString(), server.getUsername(), server.getPasswordString());
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
            facade.testServerConnection("", "", server.getPasswordString());
            fail();
        } catch (BambooLoginException e) {

        }

    }


    public void testBambooConnectionWithEmptyPlan() throws BambooLoginException, CloneNotSupportedException, ServerPasswordNotProvidedException {
        server.setSubscribedPlansData(new ArrayList<SubscribedPlanBean>());
        BambooServerFacade facade = new BambooServerFacadeImpl();
        Collection<BambooBuild> plans = facade.getSubscribedPlansResults(server);
        assertEquals(0, plans.size());
    }
}
