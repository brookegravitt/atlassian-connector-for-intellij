package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.bamboo.api.BambooLoginException;
import com.atlassian.theplugin.bamboo.api.bamboomock.*;
import com.atlassian.theplugin.configuration.*;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * {@link com.atlassian.theplugin.bamboo.BambooServerFacadeImpl} test.
 */
public class BambooServerFacadeTest extends TestCase {

	private static final String USER_NAME = "someUser";
	private static final String PASSWORD = "somePassword";
	private static final String PLAN_ID = "TP-DEF"; // always the same - mock does the logic


	private org.mortbay.jetty.Server httpServer;
	private JettyMockServer mockServer;
	private String mockBaseUrl;

	protected void setUp() throws Exception {
		httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

		mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();

		mockServer = new JettyMockServer(httpServer);
		ConfigurationFactory.setConfiguration(createBambooTestConfiguration(mockBaseUrl));
	}

	private static PluginConfiguration createBambooTestConfiguration(String serverUrl) {
		BambooConfigurationBean configuration = new BambooConfigurationBean();

		Collection<ServerBean> servers = new ArrayList<ServerBean>();
		ServerBean server = new ServerBean();

		server.setName("TestServer");
		server.setUrlString(serverUrl);
		server.setUsername(USER_NAME);
		server.setPasswordString(PASSWORD, true);
		servers.add(server);

		ArrayList<SubscribedPlanBean> plans = new ArrayList<SubscribedPlanBean>();
		for (int i = 1; i <= 3; ++i) {
			SubscribedPlanBean plan = new SubscribedPlanBean();
			plan.setPlanId(PLAN_ID);
			plans.add(plan);
		}

		server.setSubscribedPlansData(plans);

		configuration.setServersData(servers);
		PluginConfigurationBean pluginConfig = new PluginConfigurationBean();
		pluginConfig.setBambooConfigurationData(configuration);

		return pluginConfig;
	}

	protected void tearDown() throws Exception {
		mockServer = null;
		mockBaseUrl = null;
		httpServer.stop();
	}

	public void testSubscribedBuildStatus() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback());
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("FAILED"));
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("WRONG"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers().iterator().next();

		Collection<BambooBuild> plans = BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults(server);
		assertNotNull(plans);
		assertEquals(3, plans.size());
		Iterator<BambooBuild> iterator = plans.iterator();
		Util.verifySuccessfulBuildResult(iterator.next(), mockBaseUrl);
		Util.verifyFailedBuildResult(iterator.next(), mockBaseUrl);
		Util.verifyErrorBuildResult(iterator.next(), mockBaseUrl);

		mockServer.verify();
	}

	public void testFailedLoginSubscribedBuildStatus() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers().iterator().next();

		Collection<BambooBuild> plans = BambooServerFactory.getBambooServerFacade().getSubscribedPlansResults(server);
		assertNotNull(plans);
		assertEquals(3, plans.size());
		Iterator<BambooBuild> iterator = plans.iterator();
		Util.verifyLoginErrorBuildResult(iterator.next(), mockBaseUrl);
		Util.verifyLoginErrorBuildResult(iterator.next(), mockBaseUrl);
		Util.verifyLoginErrorBuildResult(iterator.next(), mockBaseUrl);

		mockServer.verify();
	}

	public void testProjectList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listProjectNames.action", new ProjectListCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers().iterator().next();

		Collection<BambooProject> projects = BambooServerFactory.getBambooServerFacade().getProjectList(server);
		Util.verifyProjectListResult(projects);

		mockServer.verify();
	}

	public void testFailedProjectList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));
		Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers().iterator().next();

		Collection<BambooProject> projects = BambooServerFactory.getBambooServerFacade().getProjectList(server);
		assertNull(projects);
		mockServer.verify();
	}

	public void testPlanList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers().iterator().next();

		Collection<BambooPlan> plans = BambooServerFactory.getBambooServerFacade().getPlanList(server);
		Util.verifyPlanListResult(plans);

		mockServer.verify();
	}

	public void testFailedPlanList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers().iterator().next();

		Collection<BambooPlan> plans = BambooServerFactory.getBambooServerFacade().getPlanList(server);
		assertNull(plans);

		mockServer.verify();
	}

	public void testConnectionTest() throws Exception {
		BambooServerFacade facade = BambooServerFactory.getBambooServerFacade();

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());
		facade.testServerConnection(mockBaseUrl, USER_NAME, PASSWORD);

		try {
			facade.testServerConnection("", "", "");
			fail();
		} catch (BambooLoginException e) {
			// expected
		}

		mockServer.expect("/api/rest/login.action", new LoginCallback("", "", LoginCallback.ALWAYS_FAIL));
		try {
			facade.testServerConnection(mockBaseUrl, "", "");
			fail();
		} catch (BambooLoginException e) {
			//expected
		}

		try {
			facade.testServerConnection("", USER_NAME, "");
			fail();
		} catch (BambooLoginException e) {
			//expected
		}

		try {
			facade.testServerConnection("", "", PASSWORD);
			fail();
		} catch (BambooLoginException e) {
			//expected
		}
		mockServer.verify();
	}


	public void testBambooConnectionWithEmptyPlan() throws BambooLoginException, CloneNotSupportedException, ServerPasswordNotProvidedException {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());
		Server server = ConfigurationFactory.getConfiguration().getBambooConfiguration().getServers().iterator().next();
        server.getSubscribedPlans().clear();
		BambooServerFacade facade = new BambooServerFacadeImpl();
        Collection<BambooBuild> plans = facade.getSubscribedPlansResults(server);
        assertEquals(0, plans.size());

		mockServer.verify();
	}
}
