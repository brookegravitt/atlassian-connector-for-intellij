package com.atlassian.theplugin.bamboo.api;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BambooPlan;
import com.atlassian.theplugin.bamboo.BambooProject;
import com.atlassian.theplugin.bamboo.api.bamboomock.*;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.mortbay.jetty.Server;

import java.util.List;


/**
 * Test case for {#link BambooSession}
 */
public class BambooSessionTest extends TestCase {
	private static final String USER_NAME = "someUser";
	private static final String PASSWORD = "somePassword";

	private Server server;
	private JettyMockServer mockServer;
	private String mockBaseUrl;

	protected void setUp() throws Exception {
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

		BambooSession apiHandler = new BambooSession(mockBaseUrl);

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
		mockServer.expect("//api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("//api/rest/logout.action", new LogoutCallback(LoginCallback.AUTH_TOKEN));

		BambooSession apiHandler = new BambooSession(mockBaseUrl + "/");
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		assertTrue(apiHandler.isLoggedIn());
		apiHandler.logout();
		assertFalse(apiHandler.isLoggedIn());

		mockServer.verify();
	}

	public void testNullParamsLogin() throws Exception {
		try {
			BambooSession apiHandler = new BambooSession(null);
			apiHandler.login(null, null);
			fail();
		} catch (BambooLoginException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}

	public void testWrongUrlBambooLogin() throws Exception {
		mockServer.expect("/wrongurl/api/rest/login.action", new ErrorResponse(400));

		try {
			BambooSession apiHandler = new BambooSession(mockBaseUrl + "/wrongurl");
			apiHandler.login(USER_NAME, PASSWORD.toCharArray());
			fail();
		} catch (BambooLoginException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
		mockServer.verify();
	}

	public void testWrongUserBambooLogin() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		try {
			BambooSession apiHandler = new BambooSession(mockBaseUrl);
			apiHandler.login(USER_NAME, PASSWORD.toCharArray()); // mock will fail this
			fail();
		} catch (BambooLoginException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}

		mockServer.verify();
	}


	public void testWrongParamsBambooLogin() throws Exception {
		try {
			BambooSession apiHandler = new BambooSession("");
			apiHandler.login("", "".toCharArray());
			fail();
		} catch (BambooLoginException ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
	}



	public void testProjectList() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/listProjectNames.action", new ProjectListCallback());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = new BambooSession(mockBaseUrl);
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

		BambooSession apiHandler = new BambooSession(mockBaseUrl);
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

		BambooSession apiHandler = new BambooSession(mockBaseUrl);
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

		BambooSession apiHandler = new BambooSession(mockBaseUrl);
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

		BambooSession apiHandler = new BambooSession(mockBaseUrl);
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

		BambooSession apiHandler = new BambooSession(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF");
		apiHandler.logout();

		Util.verifyErrorBuildResult(build, mockBaseUrl);
		
		mockServer.verify();
	}

//  commented because nobody actually uses this method, and the unit test does not really test anything, so we
//	don't even know if the method works
//	
// public void testRecentBuilds() throws Exception {
//		BambooSession apiHandler = new BambooSession(SERVER_URL);
//		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
//		List<BambooBuild> builds = apiHandler.getLatestBuildsForProject("TP");
//		assertNotNull(builds);
//		apiHandler.logout();
//	}
//
//	public void testRecentBuildsForNonExistingProject() throws Exception {
//		BambooSession apiHandler = new BambooSession(SERVER_URL);
//		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
//		try {
//			List<BambooBuild> builds = apiHandler.getLatestBuildsForProject("TP-NON-EXISTING");
//			fail();
//		} catch (BambooException e) {
//
//		}
//		apiHandler.logout();
//	}
//
//	public void testRecentBuildsForEmptyProject() throws Exception {
//		BambooSession apiHandler = new BambooSession(SERVER_URL);
//		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
//		try {
//			List<BambooBuild> builds = apiHandler.getLatestBuildsForProject("TP-NON-EXISTING");
//			fail();
//		} catch (BambooException e) {
//
//		}
//		apiHandler.logout();
//	}

}
