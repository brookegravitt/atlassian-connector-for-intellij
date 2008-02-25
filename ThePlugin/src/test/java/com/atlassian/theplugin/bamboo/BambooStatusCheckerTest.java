package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.UIActionScheduler;
import com.atlassian.theplugin.bamboo.api.bamboomock.LatestBuildResultCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.LoginCallback;
import com.atlassian.theplugin.configuration.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ddsteps.mock.httpserver.JettyMockServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TimerTask;

/**
 * BambooStatusChecker Tester.
 */
public class BambooStatusCheckerTest extends TestCase {
	private static final String USER_NAME = "someUser";
	private static final String PASSWORD = "somePassword";
	private static final String PLAN_ID = "TP-DEF"; // always the same - mock does the logic
	
	public BambooStatusCheckerTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetInterval() throws Exception {
		BambooStatusChecker checker = new BambooStatusChecker(null);
		assertEquals(20000, checker.getInterval());
	}

	public void testNewTimerTask() {
		BambooStatusChecker checker = new BambooStatusChecker(null);
		TimerTask t1 = checker.newTimerTask();
		TimerTask t2 = checker.newTimerTask();

		assertNotSame(t1, t2);

	}

	public void testLogic() throws Exception {
		// perform no-operation
		PluginConfigurationBean config = createBambooTestConfiguration();
		ConfigurationFactory.setConfiguration(config);

		EasyInvoker invoker = new EasyInvoker();
		BambooStatusChecker checker = new BambooStatusChecker(invoker);

		TimerTask task = checker.newTimerTask();
		task.run();

		assertTrue(invoker.getLastAndClear());

		MockReceiver r1 = new MockReceiver();
		MockReceiver r2 = new MockReceiver();

		checker.registerListener(r1);

		task.run();
		assertTrue(invoker.getLastAndClear());
		assertNotNull(r1.getLastAndClear());

		checker.registerListener(r2);
		task.run();
		assertNotNull(r1.getLastAndClear());
		assertNotNull(r2.getLastAndClear());

		assertNull(r1.lastStatuses);
		assertNull(r2.lastStatuses);

		checker.unregisterListener(r1);
		task.run();
		assertNull(r1.getLastAndClear());
		assertNotNull(r2.getLastAndClear());

		task = checker.newTimerTask();
		task.run();
		assertNull(r1.getLastAndClear());
		assertNotNull(r2.getLastAndClear());

		assertFalse(checker.canSchedule()); // config empty

		/************************************************************************/
		org.mortbay.jetty.Server httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();

		JettyMockServer mockServer = new JettyMockServer(httpServer);
		addServer(config, mockBaseUrl);

		assertTrue(checker.canSchedule()); // config not empty

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback());

		task.run();
		assertEquals(1, r2.lastStatuses.size());

		mockServer.verify();
		httpServer.stop();

	}


	private static PluginConfigurationBean createBambooTestConfiguration() {
		BambooConfigurationBean configuration = new BambooConfigurationBean();

		Collection<ServerBean> servers = new ArrayList<ServerBean>();
		configuration.setServersData(servers);
		PluginConfigurationBean pluginConfig = new PluginConfigurationBean();
		pluginConfig.setBambooConfigurationData(configuration);

		return pluginConfig;
	}

	private static void addServer(PluginConfigurationBean config, String url) {
		Collection<ServerBean> servers = new ArrayList<ServerBean>();
		ServerBean server = new ServerBean();

		server.setName("TestServer");
		server.setUrlString(url);
		server.setUserName(USER_NAME);

		server.setPasswordString(PASSWORD, false);
		server.setIsConfigInitialized(true);
		servers.add(server);

		ArrayList<SubscribedPlanBean> plans = new ArrayList<SubscribedPlanBean>();

		SubscribedPlanBean plan = new SubscribedPlanBean();
		plan.setPlanId(PLAN_ID);
		plans.add(plan);

		server.setSubscribedPlansData(plans);

		config.getBambooConfigurationData().setServersData(servers);

	}

	public static Test suite() {
        return new TestSuite(BambooStatusCheckerTest.class);
    }

	private class MockReceiver implements BambooStatusListener {

		private Collection<BambooBuild> lastStatuses;

		public void updateBuildStatuses(Collection<BambooBuild> buildStatuses) {
			lastStatuses = buildStatuses;
		}

		Collection<BambooBuild> getLastAndClear() {
			Collection<BambooBuild> ret = lastStatuses;
			lastStatuses = null;
			return ret;
		}
		public Collection<BambooBuild> getLastStatuses() {
			return lastStatuses;
		}
	}

	private class EasyInvoker implements UIActionScheduler {
		private boolean invoked;

		public void invokeLater(Runnable action) {
			invoked = true;
			action.run();
		}

		boolean getLastAndClear() {
			boolean ret = invoked;
			invoked = false;
			return ret;
		}
	}
}
