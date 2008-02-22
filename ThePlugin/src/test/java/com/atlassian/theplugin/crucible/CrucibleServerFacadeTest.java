package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.configuration.*;
import com.atlassian.theplugin.crucible.api.CrucibleLoginFailedException;
import com.atlassian.theplugin.crucible.api.rest.cruciblemock.LoginCallback;
import junit.framework.TestCase;
import org.ddsteps.mock.httpserver.JettyMockServer;

import java.util.ArrayList;
import java.util.Collection;

public class CrucibleServerFacadeTest extends TestCase {
	private static final String VALID_LOGIN = "validLogin";
	private static final String VALID_PASSWORD = "validPassword";
	private static final String VALID_URL = "http://localhost:9001";

	private static final String USER_NAME = "someUser";
	private static final String PASSWORD = "somePassword";

	private org.mortbay.jetty.Server httpServer;
	private JettyMockServer mockServer;
	private String mockBaseUrl;
	public static final String INVALID_PROJECT_KEY = "INVALID project key";

	protected void setUp() throws Exception {
		httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

		mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();

		mockServer = new JettyMockServer(httpServer);
		ConfigurationFactory.setConfiguration(createCrucibleTestConfiguration(mockBaseUrl, true));
	}

	private static PluginConfiguration createCrucibleTestConfiguration(String serverUrl, boolean isPassInitialized) {
		CrucibleConfigurationBean configuration = new CrucibleConfigurationBean();

		Collection<ServerBean> servers = new ArrayList<ServerBean>();
		ServerBean server = new ServerBean();

		server.setName("TestServer");
		server.setUrlString(serverUrl);
		server.setUserName(USER_NAME);

		server.setPasswordString(isPassInitialized ? PASSWORD : "", isPassInitialized);
		server.setIsConfigInitialized(isPassInitialized);
		servers.add(server);

		configuration.setServersData(servers);
		PluginConfigurationBean pluginConfig = new PluginConfigurationBean();
		pluginConfig.setCrucibleConfigurationData(configuration);

		return pluginConfig;
	}

	protected void tearDown() throws Exception {
		mockServer = null;
		mockBaseUrl = null;
		httpServer.stop();
	}

	public void testFailedLoginGetAllReviews() throws Exception {
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		Server server = ConfigurationFactory.getConfiguration().getProductServers(ServerType.CRUCIBLE_SERVER).getServers().iterator().next();
		try {
			CrucibleServerFactory.getCrucibleServerFacade().getAllReviews(server);
			fail();
		} catch (CrucibleLoginFailedException e) {

		}

		mockServer.verify();
	}
/*
	public void testConnectionTestFailed() {

		try {
			crucibleSessionMock.login("badUserName", "badPassword");
			EasyMock.expectLastCall().andThrow(new CrucibleLoginException(""));
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		replay(crucibleSessionMock);

		try {
			facade.testServerConnection(VALID_URL, "badUserName", "badPassword");
			fail("testServerConnection failed");
		} catch (CrucibleException e) {
			// testServerConnection succeed
		} finally {
			EasyMock.verify(crucibleSessionMock);
		}
	}

	public void testConnectionTestSucceed() {

		try {
			crucibleSessionMock.login("CorrectUserName", "CorrectPassword");
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		crucibleSessionMock.logout();

		replay(crucibleSessionMock);

		try {
			facade.testServerConnection(VALID_URL, "CorrectUserName", "CorrectPassword");
		} catch (CrucibleException e) {
			fail("testServerConnection failed");
		} finally {
			EasyMock.verify(crucibleSessionMock);
		}
	}

	public void testCreateReview() throws Exception {
		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		crucibleSessionMock.createReview(EasyMock.isA(ReviewData.class));
		ReviewData response = new ReviewDataInfoImpl(null, null, null);

		EasyMock.expectLastCall().andReturn(response);
		crucibleSessionMock.logout();

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		ReviewData reviewData = prepareReviewData("name", State.DRAFT);

		// test call
		ReviewData ret = facade.createReview(server, reviewData);
		assertSame(response, ret);

		EasyMock.verify(crucibleSessionMock);


	}

	public void testCreateReviewWithInvalidProjectKey() throws Exception {

		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		crucibleSessionMock.createReview(EasyMock.isA(ReviewData.class));

		EasyMock.expectLastCall().andThrow(new CrucibleException("test"));
		crucibleSessionMock.logout();

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		ReviewData reviewData = prepareReviewData("name", State.DRAFT);

		try {
			// test call
			facade.createReview(server, reviewData);
			fail("creating review with invalid key should throw an CrucibleException()");
		} catch (CrucibleException e) {

		} finally {
			EasyMock.verify(crucibleSessionMock);
		}

	}

	public void testCreateReviewFromPatch() throws ServerPasswordNotProvidedException, CrucibleException {

		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		crucibleSessionMock.createReviewFromPatch(EasyMock.isA(ReviewData.class), EasyMock.eq("some patch"));
		ReviewData response = new ReviewDataInfoImpl(null, null, null);
		EasyMock.expectLastCall().andReturn(response);
		crucibleSessionMock.logout();

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		ReviewData reviewData = prepareReviewData("name", State.DRAFT);

		String patch = "some patch";

		// test call
		ReviewData ret = facade.createReviewFromPatch(server, reviewData, patch);
		assertSame(response, ret);

		EasyMock.verify(crucibleSessionMock);
	}

	public void testCreateReviewFromPatchWithInvalidProjectKey() throws Exception {

		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		crucibleSessionMock.createReviewFromPatch(EasyMock.isA(ReviewData.class), EasyMock.eq("some patch"));
		EasyMock.expectLastCall().andThrow(new CrucibleException("test"));
		crucibleSessionMock.logout();

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		ReviewData reviewData = prepareReviewData("name", State.DRAFT);

		String patch = "some patch";

		try {
			facade.createReviewFromPatch(server, reviewData, patch);
			fail("creating review with patch with invalid key should throw an CrucibleException()");
		} catch (CrucibleException e) {
			// ignored by design
		} finally {
			EasyMock.verify(crucibleSessionMock);
		}
	}

	public void testGetAllReviews() throws ServerPasswordNotProvidedException, CrucibleException {

		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		PermId permId = new PermId() {
			public String getId() {
				return "permId";
			}
		};

		ReviewDataInfoImpl review = new ReviewDataInfoImpl(prepareReviewData("name", State.DRAFT, permId), null, null);

		crucibleSessionMock.getAllReviews();
		EasyMock.expectLastCall().andReturn(Arrays.asList(new ReviewDataInfo[]{ review, review }));

		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList(new String[]{ "Alice", "Bob", "Charlie" }));
		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList(new String[]{ "Alice", "Bob", "Charlie" }));

		crucibleSessionMock.logout();

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		// test call
		List<ReviewDataInfo> ret = facade.getAllReviews(server);
		assertEquals(2, ret.size());
		assertEquals(3, ret.get(0).getReviewers().size());
		assertEquals(permId.getId(), ret.get(0).getPermaId().getId());
		assertEquals("name", ret.get(0).getName());

		EasyMock.verify(crucibleSessionMock);
	}

	public void testGetActiveReviewsForUser() throws ServerPasswordNotProvidedException, CrucibleException {

		try {
			crucibleSessionMock.login(VALID_LOGIN, VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		PermId permId = new PermId() {
			public String getId() {
				return "permId";
			}
		};

		crucibleSessionMock.getReviewsInStates(Arrays.asList(new State[]{ State.REVIEW }));
		EasyMock.expectLastCall().andReturn(Arrays.asList(new ReviewDataInfo[]{
				new ReviewDataInfoImpl(prepareReviewData("name", State.REVIEW, permId), null, null),
				new ReviewDataInfoImpl(prepareReviewData("name", State.REVIEW, permId), null, null)}));

		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList(new String[]{ VALID_LOGIN, "Bob", "Charlie" }));
		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList(new String[]{ "Alice", "Bob", "Charlie" }));


		crucibleSessionMock.logout();

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		// test call
		List<ReviewDataInfo> ret = facade.getActiveReviewsForUser(server);
		assertEquals(1, ret.size());
		assertEquals(3, ret.get(0).getReviewers().size());
		assertEquals(permId.getId(), ret.get(0).getPermaId().getId());
		assertEquals("name", ret.get(0).getName());
		assertEquals(VALID_LOGIN, ret.get(0).getReviewers().get(0));

		EasyMock.verify(crucibleSessionMock);
	}


	private ReviewData prepareReviewData(final String name, final State state) {
		return new ReviewData() {
			public String getAuthor() {
				return VALID_LOGIN;
			}

			public String getCreator() {
				return VALID_LOGIN;
			}

			public String getDescription() {
				return "Test description";
			}

			public String getModerator() {
				return VALID_LOGIN;
			}

			public String getName() {
				return name;
			}

			public PermId getParentReview() {
				return null;
			}

			public PermId getPermaId() {
				return new PermId() {
					public String getId() {
						return "permId";
					}
				};
			}

			public String getProjectKey() {
				return "TEST";
			}

			public String getRepoName() {
				return null;
			}

			public State getState() {
				return state;
			}
		};
	}

	private ReviewData prepareReviewData(final String name, final State state, final PermId permId) {
		return new ReviewData() {
			public String getAuthor() {
				return VALID_LOGIN;
			}

			public String getCreator() {
				return VALID_LOGIN;
			}

			public String getDescription() {
				return "Test description";
			}

			public String getModerator() {
				return VALID_LOGIN;
			}

			public String getName() {
				return name;
			}

			public PermId getParentReview() {
				return null;
			}

			public PermId getPermaId() {
				return permId;
			}

			public String getProjectKey() {
				return "TEST";
			}

			public String getRepoName() {
				return null;
			}

			public State getState() {
				return state;
			}
		};
	}

	private ServerBean prepareServerBean() {
		ServerBean server = new ServerBean();
		server.setUrlString(VALID_URL);
		server.setUserName(VALID_LOGIN);
		server.setPasswordString(VALID_PASSWORD, false);
		return server;
	}

	public void _testCreateReviewHardcoded() throws ServerPasswordNotProvidedException {

		//facade.setCrucibleSession(null);

		ServerBean server = new ServerBean();
		server.setUrlString("http://lech.atlassian.pl:8060");
		server.setUserName("test");
		server.setPasswordString("test", false);

		ReviewData reviewData = prepareReviewData("test", State.DRAFT);

		ReviewData ret;

		try {
			ret = facade.createReview(server, reviewData);
			assertNotNull(ret);
			assertNotNull(ret.getPermaId());
			assertNotNull(ret.getPermaId().getId());
			assertTrue(ret.getPermaId().getId().length() > 0);
		} catch (CrucibleException e) {
			fail(e.getMessage());
		}
	}

	public void _testGetAllReviewsHardcoded() throws ServerPasswordNotProvidedException {
		//facade.setCrucibleSession(null);

		ServerBean server = new ServerBean();
		server.setUrlString("http://lech.atlassian.pl:8060");
		server.setUserName("test");
		server.setPasswordString("test", false);

		try {
			List<ReviewDataInfo> list = facade.getAllReviews(server);
			assertNotNull(list);
			assertTrue(list.size() > 0);
		} catch (CrucibleException e) {
			fail(e.getMessage());
		}
	}
	*/
}
