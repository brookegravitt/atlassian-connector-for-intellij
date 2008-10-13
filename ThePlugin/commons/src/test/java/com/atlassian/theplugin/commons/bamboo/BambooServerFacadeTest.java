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

import com.atlassian.theplugin.bamboo.api.bamboomock.*;
import com.atlassian.theplugin.commons.SubscribedPlan;
import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.remoteapi.ErrorResponse;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * {@link com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl} test.
 */
public class BambooServerFacadeTest extends TestCase {

	private static final String USER_NAME = "someUser";
	private static final String PASSWORD = "somePassword";
	private static final String PLAN_ID = "TP-DEF"; // always the same - mock does the logic

	private org.mortbay.jetty.Server httpServer;
	private JettyMockServer mockServer;
	private String mockBaseUrl;
	private BambooServerFacade testedBambooServerFacade;
	private BambooServerCfg bambooServerCfg;

	@Override
	protected void setUp() throws Exception {
		//log
		// @TODO all remove it when ConfigurationFactory is not needed by HttpClientFactory 
		ConfigurationFactory.setConfiguration(new PluginConfigurationBean());

		httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

		mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();

		mockServer = new JettyMockServer(httpServer);
		bambooServerCfg = createBambooTestConfiguration(mockBaseUrl, true);

		testedBambooServerFacade = BambooServerFacadeImpl.getInstance(LoggerImpl.getInstance());
	}

	private static BambooServerCfg createBambooTestConfiguration(String serverUrl, boolean isPassInitialized) {

		BambooServerCfg server = new BambooServerCfg("TestServer", new ServerId());
		server.setName("TestServer");
		server.setUrl(serverUrl);
		server.setUsername(USER_NAME);

		server.setPassword(isPassInitialized ? PASSWORD : "");
//		server.transientSetIsConfigInitialized(isPassInitialized);

		ArrayList<SubscribedPlan> plans = new ArrayList<SubscribedPlan>();
		for (int i = 1; i <= 3; ++i) {
			SubscribedPlan plan = new SubscribedPlan(PLAN_ID);
			plans.add(plan);
		}

		server.setPlans(plans);
		return server;
	}

	@Override
	protected void tearDown() throws Exception {
		mockServer = null;
		mockBaseUrl = null;
		httpServer.stop();
	}

	public void testSubscribedBuildStatus() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback());
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("FAILED"));
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("WRONG"));

		Collection<BambooBuild> plans = testedBambooServerFacade.getSubscribedPlansResults(bambooServerCfg);
		assertNotNull(plans);
		assertEquals(3, plans.size());
		Iterator<BambooBuild> iterator = plans.iterator();
		Util.verifySuccessfulBuildResult(iterator.next(), mockBaseUrl);
		Util.verifyFailedBuildResult(iterator.next(), mockBaseUrl);
		Util.verifyErrorBuildResult(iterator.next());

		mockServer.verify();
	}

	public void testFailedLoginSubscribedBuildStatus() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));
		bambooServerCfg.setPassword(PASSWORD);
		Collection<BambooBuild> plans = testedBambooServerFacade.getSubscribedPlansResults(bambooServerCfg);
		assertNotNull(plans);
		assertEquals(3, plans.size());
		Iterator<BambooBuild> iterator = plans.iterator();
		Util.verifyLoginErrorBuildResult(iterator.next());
		Util.verifyLoginErrorBuildResult(iterator.next());
		Util.verifyLoginErrorBuildResult(iterator.next());

		mockServer.verify();
	}

	public void testUninitializedPassword() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, "", LoginCallback.ALWAYS_FAIL));
		BambooServerCfg server = createBambooTestConfiguration(mockBaseUrl, false);
		try {
			testedBambooServerFacade.getSubscribedPlansResults(server);
			fail("Testing uninitialized password");

		} catch (ServerPasswordNotProvidedException e) {
			// ok: connection succeeded but server returned error
		}

		mockServer.expect("/api/rest/login.action", new ErrorResponse(400, ""));
		mockServer.expect("/api/rest/login.action", new ErrorResponse(400, ""));
		// connection error, just report without asking for the pass
		Collection<BambooBuild> plans = testedBambooServerFacade.getSubscribedPlansResults(server);
		assertNotNull(plans);
		assertEquals(3, plans.size());
		Iterator<BambooBuild> iterator = plans.iterator();
		Util.verifyError400BuildResult(iterator.next());
		Util.verifyError400BuildResult(iterator.next());
		Util.verifyError400BuildResult(iterator.next());

		server.setUrl("malformed");
		plans = testedBambooServerFacade.getSubscribedPlansResults(server);
		assertNotNull(plans);
		assertEquals(3, plans.size());
		iterator = plans.iterator();
		assertEquals("Malformed server URL: malformed", iterator.next().getMessage());
		assertEquals("Malformed server URL: malformed", iterator.next().getMessage());
		assertEquals("Malformed server URL: malformed", iterator.next().getMessage());

		mockServer.verify();

	}

	public void testProjectList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/listProjectNames.action", new ProjectListCallback());

		Collection<BambooProject> projects = testedBambooServerFacade.getProjectList(bambooServerCfg);
		Util.verifyProjectListResult(projects);

		mockServer.verify();
	}

	public void testFailedProjectList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		try {
			testedBambooServerFacade.getProjectList(bambooServerCfg);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		mockServer.verify();
	}

	public void testPlanList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());

		Collection<BambooPlan> plans = testedBambooServerFacade.getPlanList(bambooServerCfg);
		Util.verifyPlanListWithFavouritesResult(plans);

		mockServer.verify();
	}

	public void testFailedPlanList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		try {
			testedBambooServerFacade.getPlanList(bambooServerCfg);
			fail();
		} catch (RemoteApiLoginException e) {
			// expected exception
		}
		mockServer.verify();
	}

	public void testConnectionTest() throws Exception {
		BambooServerFacade facade = testedBambooServerFacade;

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());
		facade.testServerConnection(mockBaseUrl, USER_NAME, PASSWORD);

		try {
			facade.testServerConnection("", "", "");
			fail();
		} catch (RemoteApiMalformedUrlException e) {
			// expected
		}

		mockServer.expect("/api/rest/login.action", new LoginCallback("", "", LoginCallback.ALWAYS_FAIL));
		try {
			facade.testServerConnection(mockBaseUrl, "", "");
			fail();
		} catch (RemoteApiLoginException e) {
			//expected
		}

		try {
			facade.testServerConnection("", USER_NAME, "");
			fail();
		} catch (RemoteApiMalformedUrlException e) {
			//expected
		}

		try {
			facade.testServerConnection("", "", PASSWORD);
			fail();
		} catch (RemoteApiMalformedUrlException e) {
			//expected
		}
		mockServer.verify();
	}


	public void testBambooConnectionWithEmptyPlan() throws RemoteApiLoginException, CloneNotSupportedException, ServerPasswordNotProvidedException {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());

		bambooServerCfg.getSubscribedPlans().clear();
		BambooServerFacade facade = BambooServerFacadeImpl.getInstance(LoggerImpl.getInstance());
		Collection<BambooBuild> plans = facade.getSubscribedPlansResults(bambooServerCfg);
		assertEquals(0, plans.size());

		mockServer.verify();
	}

	public void testAddLabel() throws Exception {
		String label = "label";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label));

		testedBambooServerFacade.addLabelToBuild(bambooServerCfg, "TP-DEF", "100", label);

		mockServer.verify();
	}

	public void testAddEmptyLabel() throws Exception {
		String label = "";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label));

		testedBambooServerFacade.addLabelToBuild(bambooServerCfg, "TP-DEF", "100", label);

		mockServer.verify();
	}

	public void testAddLabelToNonExistingBuild() throws Exception {
		String label = "label";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label, "200", AddLabelToBuildCallback.NON_EXIST_FAIL));

		try {
			testedBambooServerFacade.addLabelToBuild(bambooServerCfg, "TP-DEF", "200", label);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testAddComment() throws Exception {
		String label = "label";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(label));

		testedBambooServerFacade.addCommentToBuild(bambooServerCfg, "TP-DEF", "100", label);

		mockServer.verify();
	}

	public void testAddEmptyComment() throws Exception {
		String label = "";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(label));

		testedBambooServerFacade.addCommentToBuild(bambooServerCfg, "TP-DEF", "100", label);

		mockServer.verify();
	}

	public void testAddCommentToNonExistingBuild() throws Exception {
		String label = "label";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(label, "200", AddCommentToBuildCallback.NON_EXIST_FAIL));

		try {
			testedBambooServerFacade.addCommentToBuild(bambooServerCfg, "TP-DEF", "200", label);
			fail();
		} catch (RemoteApiException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testExecuteBuild() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/executeBuild.action", new ExecuteBuildCallback());

		testedBambooServerFacade.executeBuild(bambooServerCfg, "TP-DEF");

		mockServer.verify();
	}

	public void testFailedExecuteBuild() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/executeBuild.action", new ExecuteBuildCallback(ExecuteBuildCallback.NON_EXIST_FAIL));

		try {
			testedBambooServerFacade.executeBuild(bambooServerCfg, "TP-DEF");
		} catch (RemoteApiException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testGetBuildDetails() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("buildResult-3Commit-FailedTests-SuccessfulTests.xml", "100"));

		BuildDetails details = testedBambooServerFacade.getBuildDetails(bambooServerCfg, "TP-DEF", "100");
		assertEquals(3, details.getCommitInfo().size());
		assertEquals(2, details.getFailedTestDetails().size());
		assertEquals(117, details.getSuccessfulTestDetails().size());

		mockServer.verify();
	}

	public void testGetBuildDetailsNonExistingBuild() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("buildNotExistsResponse.xml", "200"));

		try {
			testedBambooServerFacade.getBuildDetails(bambooServerCfg, "TP-DEF", "200");
			fail();
		} catch (RemoteApiException e) {
			// expected
		}


		mockServer.verify();
	}


}
