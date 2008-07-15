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

package com.atlassian.theplugin.bamboo;

import com.atlassian.theplugin.commons.bamboo.api.BambooSession;
import com.atlassian.theplugin.commons.bamboo.api.BambooSessionImpl;
import com.atlassian.theplugin.commons.bamboo.api.AutoRenewBambooSession;
import com.atlassian.theplugin.commons.bamboo.*;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.bamboo.api.bamboomock.*;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.mortbay.jetty.Server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.List;


/**
 * Test case for {#link BambooSessionImpl}
 */
public class BambooSessionTest extends TestCase {
	private static final String USER_NAME = "someUser";
	private static final String PASSWORD = "somePassword";

	private Server server;
	private JettyMockServer mockServer;
	private String mockBaseUrl;

	protected void setUp() throws Exception {
        ConfigurationFactory.setConfiguration(new PluginConfigurationBean());

        server = new Server(0);
		server.start();

		mockBaseUrl = "http://localhost:" + server.getConnectors()[0].getLocalPort();

		mockServer = new JettyMockServer(server);
    }

	protected void tearDown() throws Exception {
		mockServer = null;
		mockBaseUrl = null;
		server.stop();
	}

	public void testSuccessBambooLogin() throws Exception {

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);

		String[] usernames = { "user", "+-=&;<>", "", "a;&username=other", "!@#$%^&*()_-+=T " };
		String[] passwords = { "password", "+-=&;<>", "", "&password=other", ",./';[]\t\\ |}{\":><?" };

		for (int i = 0; i < usernames.length; ++i) {
			mockServer.expect("/api/rest/login.action", new LoginCallback(usernames[i], passwords[i]));
			mockServer.expect("/api/rest/logout.action", new LogoutCallback());

            apiHandler.login(usernames[i], passwords[i].toCharArray());
			assertTrue(apiHandler.isLoggedIn());
			apiHandler.logout();
			assertFalse(apiHandler.isLoggedIn());
		}

		mockServer.verify();
	}

	public void testSuccessBambooLoginURLWithSlash() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback(LoginCallback.AUTH_TOKEN));

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl + "/");
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		assertTrue(apiHandler.isLoggedIn());
		apiHandler.logout();
		assertFalse(apiHandler.isLoggedIn());

		mockServer.verify();
	}

	public void testNullParamsLogin() throws Exception {
		try {
			BambooSession apiHandler = new BambooSessionImpl(null);
			apiHandler.login(null, null);
			fail();
		} catch (RemoteApiException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	public void testWrongUrlBambooLogin() throws Exception {
		ErrorResponse error = new ErrorResponse(400, "Bad Request");
		mockServer.expect("/wrongurl/api/rest/login.action", error);
		RemoteApiLoginException exception = null;

		try {
			BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl + "/wrongurl");
			apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		} catch (RemoteApiLoginException ex) {
			exception = ex;
		}
		mockServer.verify();

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertSame(IOException.class, exception.getCause().getClass());
		assertTrue(exception.getMessage().contains(error.getErrorMessage()));
	}

	public void testNonExistingServerBambooLogin() throws Exception {
		RemoteApiLoginException exception = null;

		try {
			BambooSession apiHandler = new BambooSessionImpl("http://non.existing.server.utest");
			apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		} catch (RemoteApiLoginException ex) {
			exception = ex;
		}

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertSame("UnknownHostException expected", UnknownHostException.class, exception.getCause().getClass());
		assertEquals("Checking exception message", "Unknown host: non.existing.server.utest", exception.getMessage());
	}

	public void testMalformedUrlBambooLogin() {
		tryMalformedUrl("noprotocol.url/path");
		tryMalformedUrl("http:localhost/path");
		tryMalformedUrl("http:/localhost/path");
		tryMalformedUrl("http:///localhost/path");
		tryMalformedUrl("http:localhost");
		tryMalformedUrl("http:/localhost");
		tryMalformedUrl("http:///localhost");
		tryMalformedUrl("http://");
		tryMalformedUrl("ncxvx:/localhost/path");
		tryMalformedUrl("ncxvx:///localhost/path");
		tryMalformedUrl("ncxvx://localhost/path");
		tryMalformedUrl("ncxvx:///localhost/path");
		tryMalformedUrl("https:localhost/path");
		tryMalformedUrl("https:/localhost/path");
		tryMalformedUrl("https:///localhost/path");
		tryMalformedUrl("https:localhost");
		tryMalformedUrl("https:/localhost");
		tryMalformedUrl("https:///localhost");
		tryMalformedUrl("https://");
		tryMalformedUrl("http::localhost/path");
		tryMalformedUrl("http://loca:lhost/path");
	}

	private void tryMalformedUrl(final String url) {
		RemoteApiLoginException exception = null;
		try {
			BambooSession apiHandler = new BambooSessionImpl(url);
			apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		} catch (RemoteApiException e) {
			exception = new RemoteApiLoginException(e.getMessage(), e);
		}

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertTrue("RemoteApiException expected", exception.getCause() instanceof RemoteApiException);
		assertTrue("MalformedURLExceptionException expected", exception.getCause().getCause() instanceof MalformedURLException);
		assertEquals("Malformed server URL: " + url, exception.getMessage());
	}

	public void testWrongUserBambooLogin() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		try {
			BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
			apiHandler.login(USER_NAME, PASSWORD.toCharArray()); // mock will fail this
			fail();
		} catch (RemoteApiLoginException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}

		mockServer.verify();
	}


	public void testWrongParamsBambooLogin() throws Exception {
		try {
			BambooSession apiHandler = new BambooSessionImpl("");
			apiHandler.login("", "".toCharArray());
			fail();
		} catch (RemoteApiException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}


	public void testProjectList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listProjectNames.action", new ProjectListCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		List<BambooProject> projects = apiHandler.listProjectNames();
		apiHandler.logout();

		Util.verifyProjectListResult(projects);

		mockServer.verify();
	}

	public void testPlanList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		List<BambooPlan> plans = apiHandler.listPlanNames();
		apiHandler.logout();

		Util.verifyPlanListResult(plans);
		mockServer.verify();
	}

	public void testFavouritePlanList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		List<String> plans = apiHandler.getFavouriteUserPlans();
		apiHandler.logout();

		Util.verifyFavouriteListResult(plans);
		mockServer.verify();
	}

	public void testBuildForPlanSuccess() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF");
		apiHandler.logout();

		Util.verifySuccessfulBuildResult(build, mockBaseUrl);

		mockServer.verify();
	}


	public void testBuildForPlanFailure() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("FAILED"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF");
		apiHandler.logout();

		Util.verifyFailedBuildResult(build, mockBaseUrl);

		mockServer.verify();
	}

	public void testBuildForNonExistingPlan() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("WRONG"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF");
		apiHandler.logout();

		Util.verifyErrorBuildResult(build);

		mockServer.verify();
	}

	public void testBuildDetailsFor1CommitFailedSuccessTests() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("buildResult-1Commit-FailedTests-SuccessfulTests.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", "100");
		apiHandler.logout();

		mockServer.verify();

		assertNotNull(build);
		assertEquals("13928", build.getVcsRevisionKey());
		// commit
		assertEquals(1, build.getCommitInfo().size());
		assertEquals("author", build.getCommitInfo().iterator().next().getAuthor());
		assertNotNull(build.getCommitInfo().iterator().next().getCommitDate());
		assertEquals("commit comment", build.getCommitInfo().iterator().next().getComment());
		assertEquals(3, build.getCommitInfo().iterator().next().getFiles().size());
		assertEquals("13928", build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getRevision());
		assertEquals(
				"/PL/trunk/ThePlugin/src/main/java/com/atlassian/theplugin/bamboo/HtmlBambooStatusListener.java",
				build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getName());

		// failed tests
		assertEquals(2, build.getFailedTestDetails().size());
		assertEquals("com.atlassian.theplugin.bamboo.HtmlBambooStatusListenerTest",
				build.getFailedTestDetails().iterator().next().getTestClassName());
		assertEquals("testSingleSuccessResultForDisabledBuild",
				build.getFailedTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.012,
				build.getFailedTestDetails().iterator().next().getTestDuration());
		assertNotNull(build.getFailedTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_FAILED,
				build.getFailedTestDetails().iterator().next().getTestResult());

		// successful tests
		assertEquals(117, build.getSuccessfulTestDetails().size());
		assertEquals("com.atlassian.theplugin.bamboo.BambooServerFacadeTest",
				build.getSuccessfulTestDetails().iterator().next().getTestClassName());
		assertEquals("testProjectList",
				build.getSuccessfulTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.046,
				build.getSuccessfulTestDetails().iterator().next().getTestDuration());
		assertNull(build.getSuccessfulTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_SUCCEED,
				build.getSuccessfulTestDetails().iterator().next().getTestResult());
	}

	public void testBuildDetailsFor1CommitFailedTests() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("buildResult-1Commit-FailedTests.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", "100");
		apiHandler.logout();
		
		mockServer.verify();

		assertNotNull(build);
		assertEquals("13928", build.getVcsRevisionKey());
		// commit
		assertEquals(1, build.getCommitInfo().size());
		assertEquals("author", build.getCommitInfo().iterator().next().getAuthor());
		assertNotNull(build.getCommitInfo().iterator().next().getCommitDate());
		assertEquals("commit comment", build.getCommitInfo().iterator().next().getComment());
		assertEquals(3, build.getCommitInfo().iterator().next().getFiles().size());
		assertEquals("13928", build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getRevision());
		assertEquals(
				"/PL/trunk/ThePlugin/src/main/java/com/atlassian/theplugin/bamboo/HtmlBambooStatusListener.java",
				build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getName());

		// failed tests
		assertEquals(2, build.getFailedTestDetails().size());
		assertEquals("com.atlassian.theplugin.bamboo.HtmlBambooStatusListenerTest",
				build.getFailedTestDetails().iterator().next().getTestClassName());
		assertEquals("testSingleSuccessResultForDisabledBuild",
				build.getFailedTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.012,
				build.getFailedTestDetails().iterator().next().getTestDuration());
		assertNotNull(build.getFailedTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_FAILED,
				build.getFailedTestDetails().iterator().next().getTestResult());

		// successful tests
		assertEquals(0, build.getSuccessfulTestDetails().size());
	}

	public void testBuildDetailsFor1CommitSuccessTests() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("buildResult-1Commit-SuccessfulTests.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", "100");
		apiHandler.logout();

		mockServer.verify();

		assertNotNull(build);
		assertEquals("13928", build.getVcsRevisionKey());
		// commit
		assertEquals(1, build.getCommitInfo().size());
		assertEquals("author", build.getCommitInfo().iterator().next().getAuthor());
		assertNotNull(build.getCommitInfo().iterator().next().getCommitDate());
		assertEquals("commit comment", build.getCommitInfo().iterator().next().getComment());
		assertEquals(3, build.getCommitInfo().iterator().next().getFiles().size());
		assertEquals("13928", build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getRevision());
		assertEquals(
				"/PL/trunk/ThePlugin/src/main/java/com/atlassian/theplugin/bamboo/HtmlBambooStatusListener.java",
				build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getName());

		// failed tests
		assertEquals(0, build.getFailedTestDetails().size());

		// successful tests
		assertEquals(117, build.getSuccessfulTestDetails().size());
		assertEquals("com.atlassian.theplugin.bamboo.BambooServerFacadeTest",
				build.getSuccessfulTestDetails().iterator().next().getTestClassName());
		assertEquals("testProjectList",
				build.getSuccessfulTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.046,
				build.getSuccessfulTestDetails().iterator().next().getTestDuration());
		assertNull(build.getSuccessfulTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_SUCCEED,
				build.getSuccessfulTestDetails().iterator().next().getTestResult());
	}

	public void testBuildDetailsFor3CommitFailedSuccessTests() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("buildResult-3Commit-FailedTests-SuccessfulTests.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", "100");
		apiHandler.logout();

		mockServer.verify();

		assertNotNull(build);
		assertEquals("13928", build.getVcsRevisionKey());
		// commit
		assertEquals(3, build.getCommitInfo().size());
		assertEquals("author", build.getCommitInfo().get(0).getAuthor());
		assertNotNull(build.getCommitInfo().get(0).getCommitDate());
		assertEquals("commit comment", build.getCommitInfo().get(0).getComment());
		assertEquals(3, build.getCommitInfo().get(0).getFiles().size());
		assertEquals("13928", build.getCommitInfo().get(0).getFiles().iterator().next().getFileDescriptor().getRevision());
		assertEquals(
				"/PL/trunk/ThePlugin/src/main/java/com/atlassian/theplugin/bamboo/HtmlBambooStatusListener.java",
				build.getCommitInfo().get(0).getFiles().iterator().next().getFileDescriptor().getName());
		assertEquals(2, build.getCommitInfo().get(1).getFiles().size());
		assertEquals(1, build.getCommitInfo().get(2).getFiles().size());		

		// failed tests
		assertEquals(2, build.getFailedTestDetails().size());
		assertEquals("com.atlassian.theplugin.bamboo.HtmlBambooStatusListenerTest",
				build.getFailedTestDetails().iterator().next().getTestClassName());
		assertEquals("testSingleSuccessResultForDisabledBuild",
				build.getFailedTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.012,
				build.getFailedTestDetails().iterator().next().getTestDuration());
		assertNotNull(build.getFailedTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_FAILED,
				build.getFailedTestDetails().iterator().next().getTestResult());

		assertEquals("error 1\n", build.getFailedTestDetails().get(0).getErrors());
		assertEquals("error 2\n", build.getFailedTestDetails().get(1).getErrors());		

		// successful tests
		assertEquals(117, build.getSuccessfulTestDetails().size());
		assertEquals("com.atlassian.theplugin.bamboo.BambooServerFacadeTest",
				build.getSuccessfulTestDetails().iterator().next().getTestClassName());
		assertEquals("testProjectList",
				build.getSuccessfulTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.046,
				build.getSuccessfulTestDetails().iterator().next().getTestDuration());
		assertNull(build.getSuccessfulTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_SUCCEED,
				build.getSuccessfulTestDetails().iterator().next().getTestResult());

		assertEquals("com.atlassian.theplugin.crucible.CrucibleServerFacadeConnectionTest",
				build.getSuccessfulTestDetails().get(116).getTestClassName());
		assertEquals("testConnectionTestFailedNullPassword",
				build.getSuccessfulTestDetails().get(116).getTestMethodName());
		assertEquals(0.001,
				build.getSuccessfulTestDetails().get(116).getTestDuration());
		assertNull(build.getSuccessfulTestDetails().get(116).getErrors());
		assertEquals(TestResult.TEST_SUCCEED,
				build.getSuccessfulTestDetails().get(116).getTestResult());
	}

	public void testBuildDetailsForNoCommitFailedSuccessTests() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("buildResult-NoCommit-FailedTests-SuccessfulTests.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", "100");
		apiHandler.logout();

		mockServer.verify();

		assertNotNull(build);
		assertEquals("13928", build.getVcsRevisionKey());
		// commit
		assertEquals(0, build.getCommitInfo().size());

		// failed tests
		assertEquals(2, build.getFailedTestDetails().size());
		assertEquals("com.atlassian.theplugin.bamboo.HtmlBambooStatusListenerTest",
				build.getFailedTestDetails().iterator().next().getTestClassName());
		assertEquals("testSingleSuccessResultForDisabledBuild",
				build.getFailedTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.012,
				build.getFailedTestDetails().iterator().next().getTestDuration());
		assertNotNull(build.getFailedTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_FAILED,
				build.getFailedTestDetails().iterator().next().getTestResult());

		// successful tests
		assertEquals(117, build.getSuccessfulTestDetails().size());
		assertEquals("com.atlassian.theplugin.bamboo.BambooServerFacadeTest",
				build.getSuccessfulTestDetails().iterator().next().getTestClassName());
		assertEquals("testProjectList",
				build.getSuccessfulTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.046,
				build.getSuccessfulTestDetails().iterator().next().getTestDuration());
		assertNull(build.getSuccessfulTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_SUCCEED,
				build.getSuccessfulTestDetails().iterator().next().getTestResult());
	}

	public void testBuildDetailsForNonExistingBuild() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("buildNotExistsResponse.xml", "200"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		try {
			apiHandler.getBuildResultDetails("TP-DEF", "200");
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		apiHandler.logout();

		mockServer.verify();
	}

	public void testBuildDetailsMalformedResponse() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("malformedBuildResult.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		try {
			BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", "100");
			fail();
		} catch (RemoteApiException e) {
			assertEquals("org.jdom.input.JDOMParseException", e.getCause().getClass().getName());
		}
		apiHandler.logout();

		mockServer.verify();
	}

	public void testBuildDetailsEmptyResponse() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBuildResultsDetails.action", new BuildDetailsResultCallback("emptyResponse.xml", "100"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", "100");
		apiHandler.logout();

		assertEquals(0, build.getCommitInfo().size());
		assertEquals(0, build.getSuccessfulTestDetails().size());
		assertEquals(0, build.getFailedTestDetails().size());

		mockServer.verify();
	}

	public void testAddSimpleLabel() throws Exception {
		String label = "label siple text";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.addLabelToBuild("TP-DEF", "100", label);
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddEmptyLabel() throws Exception {
		String label = "";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.addLabelToBuild("TP-DEF", "100", label);
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddMultiLineLabel() throws Exception {
		String label = "Label first line\nLabel second line	\nLabel third line";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.addLabelToBuild("TP-DEF", "100", label);
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddLabelToNonExistingBuild() throws Exception {
		String label = "Label";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label, "200", AddLabelToBuildCallback.NON_EXIST_FAIL));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		try {
			apiHandler.addLabelToBuild("TP-DEF", "200", label);
			fail();
		} catch (RemoteApiException e) {

		}
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddComment() throws Exception {
		String comment = "comment siple text";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(comment));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.addCommentToBuild("TP-DEF", "100", comment);
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddEmptyComment() throws Exception {
		String comment = "";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(comment));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.addCommentToBuild("TP-DEF", "100", comment);
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddMultiLineComment() throws Exception {
		String comment = "Comment first line\nComment ; second line	\nComment third line";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(comment));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.addCommentToBuild("TP-DEF", "100", comment);
		apiHandler.logout();

		mockServer.verify();
	}

	public void testAddCommentToNonExistingBuild() throws Exception {
		String comment = "Comment";

		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(comment, "200", AddCommentToBuildCallback.NON_EXIST_FAIL));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		try {
			apiHandler.addCommentToBuild("TP-DEF", "200", comment);
			fail();
		} catch (RemoteApiException e) {

		}
		apiHandler.logout();

		mockServer.verify();
	}

	public void testExecuteBuild() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/executeBuild.action", new ExecuteBuildCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.executeBuild("TP-DEF");
		apiHandler.logout();

		mockServer.verify();
	}

	public void testExecuteBuildFailed() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/executeBuild.action", new ExecuteBuildCallback(ExecuteBuildCallback.NON_EXIST_FAIL));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSessionImpl(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		try {
			apiHandler.executeBuild("TP-DEF");
			fail();
		} catch (RemoteApiException e) {
			// expected
		}
		apiHandler.logout();

		mockServer.verify();
	}

	public void testRenewSession() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listProjectNames.action", new ProjectListCallback());
		mockServer.expect("/api/rest/listProjectNames.action", new ErrorMessageCallback("authExpiredResponse.xml"));
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listProjectNames.action", new ProjectListCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new AutoRenewBambooSession(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		apiHandler.listProjectNames();
		List<BambooProject> projects = apiHandler.listProjectNames();		
		apiHandler.logout();

		Util.verifyProjectListResult(projects);

		mockServer.verify();
	}

	public void testOutOfRangePort() {
		String url = "http://localhost:80808";
		RemoteApiLoginException exception = null;
		try {
			BambooSession apiHandler = new BambooSessionImpl(url);
			apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		} catch (RemoteApiException e) {
			exception = new RemoteApiLoginException(e.getMessage(), e);
		}

		assertNotNull("Exception expected", exception);
		assertNotNull("Exception should have a cause", exception.getCause());
		assertTrue("MalformedURLException expected", exception.getCause().getCause() instanceof IOException);
	}

}
