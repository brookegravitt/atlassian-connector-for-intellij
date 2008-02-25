package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.crucible.api.*;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CrucibleServerFacadeTest extends TestCase {
	private static final String VALID_LOGIN = "validLogin";
	private static final String VALID_PASSWORD = "validPassword";
	private static final String VALID_URL = "http://localhost:9001";

	private CrucibleServerFacade facade;
	private CrucibleSession crucibleSessionMock;
	public static final String INVALID_PROJECT_KEY = "INVALID project key";

	@SuppressWarnings("unchecked")
	protected void setUp() {

		crucibleSessionMock = createMock(CrucibleSession.class);

		facade = CrucibleServerFactory.getCrucibleServerFacade();

		try {
			Field f = CrucibleServerFacadeImpl.class.getDeclaredField("sessions");
			f.setAccessible(true);

			((Map<String, CrucibleSession>) f.get(facade)).put(VALID_URL, crucibleSessionMock);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

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
		EasyMock.expectLastCall().andReturn(Arrays.asList(review, review));

		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList("Alice", "Bob", "Charlie"));
		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList("Alice", "Bob", "Charlie"));

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		// test call
		List<ReviewDataInfo> ret = facade.getAllReviews(server);
		assertEquals(2, ret.size());
		assertEquals(3, ret.get(0).getReviewers().size());
		assertEquals(permId.getId(), ret.get(0).getPermaId().getId());
		assertEquals("name", ret.get(0).getName());
		assertEquals(VALID_LOGIN, ret.get(0).getAuthor());
		assertEquals(VALID_LOGIN, ret.get(0).getCreator());
		assertEquals("Test description", ret.get(0).getDescription());
		assertEquals(VALID_LOGIN, ret.get(0).getModerator());
		assertEquals("TEST", ret.get(0).getProjectKey());
		assertEquals(null, ret.get(0).getRepoName());
		assertEquals(Arrays.asList("Alice", "Bob", "Charlie"), ret.get(0).getReviewers());
		assertEquals(VALID_URL + "/cru/permId", ret.get(0).getReviewUrl());
		assertSame(server, ret.get(0).getServer());
		assertSame(State.DRAFT, ret.get(0).getState());
		assertNull(ret.get(0).getParentReview());
		
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

		crucibleSessionMock.getReviewsInStates(Arrays.asList(State.REVIEW));
		EasyMock.expectLastCall().andReturn(Arrays.asList(
				new ReviewDataInfoImpl(prepareReviewData("name", State.REVIEW, permId), null, null),
				new ReviewDataInfoImpl(prepareReviewData("name", State.REVIEW, permId), null, null)));

		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList(VALID_LOGIN, "Bob", "Charlie"));
		crucibleSessionMock.getReviewers(permId);
		EasyMock.expectLastCall().andReturn(Arrays.asList("Alice", "Bob", "Charlie"));

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
}
