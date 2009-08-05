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

package com.atlassian.theplugin.commons.bamboo;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.intellij.bamboo.BambooStatusChecker;
import com.atlassian.connector.intellij.bamboo.BambooStatusListener;
import com.atlassian.theplugin.bamboo.api.bamboomock.*;
import com.atlassian.theplugin.commons.UIActionScheduler;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooServerData;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.SubscribedPlan;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.easymock.EasyMock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TimerTask;

/**
 * BambooStatusChecker Tester.
 */
public class BambooStatusCheckerTest extends TestCase {
	private static final String USER_NAME = "someUser";

	private static final String PASSWORD = "somePassword";

	private static final String PLAN_ID = "TP-DEF"; // always the same - mock does the logic

	private Logger logger;

	public BambooStatusCheckerTest(String name) {
		super(name);

	}

	@Override
	protected void setUp() throws Exception {
		logger = EasyMock.createMock(Logger.class);
		ConfigurationFactory.setConfiguration(new PluginConfigurationBean());
	}

	public void testGetInterval() throws Exception {
		BambooStatusChecker checker = new BambooStatusChecker(
//				null, MockBambooCfgManager.createBambooTestConfiguration(),
				null, Mockito.mock(ProjectCfgManager.class), new PluginConfigurationBean(), null, logger);
		assertEquals(1000 * 60 * 10, checker.getInterval());
	}

	public void testNewTimerTask() {
		BambooStatusChecker checker = new BambooStatusChecker(null, null, null, null, logger);
		TimerTask t1 = checker.newTimerTask();
		TimerTask t2 = checker.newTimerTask();

		assertNotSame(t1, t2);

	}

	public void testLogic() throws Exception {
		// perform no-operation

		ProjectCfgManager cfg = Mockito.mock(ProjectCfgManager.class);

		EasyInvoker invoker = new EasyInvoker();
		BambooStatusChecker checker = new BambooStatusChecker(null, cfg, null, null, logger);

		checker.setActionScheduler(invoker);
		checker.updateConfiguration(cfg);

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
//		addServer(config, mockBaseUrl);
		final BambooServerCfg s = getServer(mockBaseUrl);
		final BambooServerData server = new BambooServerData(s, new UserCfg(s.getUserName(), s.getPassword()));
		Mockito.when(cfg.getAllEnabledBambooServerss()).thenReturn(Arrays.asList(server));

		assertTrue(checker.canSchedule()); // config not empty

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback());

		task.run();
		assertEquals(1, r2.lastStatuses.size());

		mockServer.verify();
		httpServer.stop();
	}

	public void testLoginNew() throws Exception {
		ProjectCfgManager cfg = Mockito.mock(ProjectCfgManager.class);
		BambooStatusChecker checker = new BambooStatusChecker(null, cfg, null, null, logger);
		TimerTask task = checker.newTimerTask();

		org.mortbay.jetty.Server httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

		String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();

		JettyMockServer mockServer = new JettyMockServer(httpServer);
		final BambooServerCfg s = getServer(mockBaseUrl);
		final BambooServerData server = new BambooServerData(s, new UserCfg(s.getUserName(), s.getPassword()));
		s.setIsBamboo2M9(true);
		Mockito.when(cfg.getAllEnabledBambooServerss()).thenReturn(Arrays.asList(server));

		assertTrue(checker.canSchedule()); // config not empty

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());
		mockServer.expect("/rest/api/latest/plan/TP-DEF", new LatestBuildResultCallbackNew());

		task.run();
		mockServer.verify();

		httpServer.stop();
	}

	private static BambooServerCfg getServer(String url) {
		BambooServerCfg server = new BambooServerCfg("TestServer", new ServerIdImpl());
		server.setUrl(url);
		server.setUsername(USER_NAME);

		server.setPassword(PASSWORD);

		ArrayList<SubscribedPlan> plans = new ArrayList<SubscribedPlan>();
		SubscribedPlan plan = new SubscribedPlan(PLAN_ID);
		plans.add(plan);

		server.setPlans(plans);
		return server;
	}

	public static Test suite() {
		return new TestSuite(BambooStatusCheckerTest.class);
	}

	private class MockReceiver implements BambooStatusListener {

		private Collection<BambooBuildAdapterIdea> lastStatuses;

		public void updateBuildStatuses(Collection<BambooBuildAdapterIdea> buildStatuses, Collection<Exception> generalExceptions) {
			lastStatuses = buildStatuses;
		}

		Collection<BambooBuildAdapterIdea> getLastAndClear() {
			Collection<BambooBuildAdapterIdea> ret = lastStatuses;
			lastStatuses = null;
			return ret;
		}

		public void resetState() {
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
