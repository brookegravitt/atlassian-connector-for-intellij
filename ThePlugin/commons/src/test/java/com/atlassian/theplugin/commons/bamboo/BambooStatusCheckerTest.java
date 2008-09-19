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

import com.atlassian.theplugin.bamboo.api.bamboomock.BamboBuildNumberCalback;
import com.atlassian.theplugin.bamboo.api.bamboomock.FavouritePlanListCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.LatestBuildResultCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.LoginCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.PlanListCallback;
import com.atlassian.theplugin.commons.SubscribedPlan;
import com.atlassian.theplugin.commons.UIActionScheduler;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.util.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.easymock.EasyMock;

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
				new ProjectId(), null, MockBambooCfgManager.createBambooTestConfiguration(), null, null, logger);
		assertEquals(60000, checker.getInterval());
	}

	public void testNewTimerTask() {
		BambooStatusChecker checker = new BambooStatusChecker(new ProjectId(), null, null, null, null, logger);
		TimerTask t1 = checker.newTimerTask();
		TimerTask t2 = checker.newTimerTask();

		assertNotSame(t1, t2);

	}

	public void testLogic() throws Exception {
		// perform no-operation

		MockBambooCfgManager config = MockBambooCfgManager.createEmptyBambooTestConfiguration();

		EasyInvoker invoker = new EasyInvoker();
		BambooStatusChecker checker = new BambooStatusChecker(new ProjectId(), null, null, null, null, logger);

		checker.setActionScheduler(invoker);
		checker.updateConfiguration(config);

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
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback());

		task.run();
		assertEquals(1, r2.lastStatuses.size());

		mockServer.verify();
		httpServer.stop();

	}


	private static void addServer(MockBambooCfgManager config, String url) {
		BambooServerCfg server = new BambooServerCfg("TestServer", new ServerId());
		server.setUrl(url);
		server.setUsername(USER_NAME);

		server.setPassword(PASSWORD);

		ArrayList<SubscribedPlan> plans = new ArrayList<SubscribedPlan>();
		SubscribedPlan plan = new SubscribedPlan(PLAN_ID);
		plans.add(plan);

		server.setPlans(plans);
		config.addServer(server);
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

		public void resetState() {
			//To change body of implemented methods use File | Settings | File Templates.
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
