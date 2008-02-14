package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.configuration.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.crucible.api.CrucibleException;
import com.atlassian.theplugin.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.crucible.api.CrucibleSession;
import com.atlassian.theplugin.crucible.api.CxfReviewServiceMockImpl;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.RpcReviewServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.State;
import junit.framework.TestCase;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 16:54:14
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleServerFacadeTest extends TestCase {
	private static final String VALID_URL = CxfReviewServiceMockImpl.VALID_URL;

	private CrucibleServerFacade facade;
	private CrucibleSession crucibleSessionMock;
	public static final String INVALID_PROJECT_KEY = "INVALID project key";

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

		CxfReviewServiceMockImpl reviewServiceMock = new CxfReviewServiceMockImpl();

		JaxWsServerFactoryBean serverFactory = new JaxWsServerFactoryBean();
		serverFactory.setServiceClass(RpcReviewServiceName.class);
		serverFactory.setAddress(VALID_URL + "/service/review");
		serverFactory.setServiceBean(reviewServiceMock);
		serverFactory.create();

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
			crucibleSessionMock.login(CxfReviewServiceMockImpl.VALID_LOGIN, CxfReviewServiceMockImpl.VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		crucibleSessionMock.createReview(EasyMock.isA(ReviewData.class));
		ReviewData response = new ReviewData();

		EasyMock.expectLastCall().andReturn(response);
		crucibleSessionMock.logout();

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		ReviewData reviewData = prepareReviewData();

		// test call
		ReviewData ret = facade.createReview(server, reviewData);
		assertSame(response, ret);

		EasyMock.verify(crucibleSessionMock);


	}

	public void testCreateReviewWithInvalidProjectKey() throws Exception {

		try {
			crucibleSessionMock.login(CxfReviewServiceMockImpl.VALID_LOGIN, CxfReviewServiceMockImpl.VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		crucibleSessionMock.createReview(EasyMock.isA(ReviewData.class));
		ReviewData response = new ReviewData();

		EasyMock.expectLastCall().andThrow(new CrucibleException("test"));
		crucibleSessionMock.logout();

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		ReviewData reviewData = prepareReviewData();

		ReviewData ret;

		try {
			reviewData.setProjectKey(INVALID_PROJECT_KEY);
			// test call
			ret = facade.createReview(server, reviewData);


			fail("creating review with invalid key should throw an CrucibleException()");

		} catch (CrucibleException e) {

		} finally {
			EasyMock.verify(crucibleSessionMock);
		}

	}

	public void testCreateReviewFromPatch() throws ServerPasswordNotProvidedException, CrucibleException {

		try {
			crucibleSessionMock.login(CxfReviewServiceMockImpl.VALID_LOGIN, CxfReviewServiceMockImpl.VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		crucibleSessionMock.createReviewFromPatch(EasyMock.isA(ReviewData.class), EasyMock.eq("some patch"));
		ReviewData response = new ReviewData();

		EasyMock.expectLastCall().andReturn(response);
		crucibleSessionMock.logout();

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		ReviewData reviewData = prepareReviewData();

		String patch = "some patch";

		// test call
		ReviewData ret = facade.createReviewFromPatch(server, reviewData, patch);
		assertSame(response, ret);

		EasyMock.verify(crucibleSessionMock);
	}

	public void testCreateReviewFromPatchWithInvalidProjectKey() throws Exception {

		try {
			crucibleSessionMock.login(CxfReviewServiceMockImpl.VALID_LOGIN, CxfReviewServiceMockImpl.VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("recording mock failed for login");
		}

		crucibleSessionMock.createReviewFromPatch(EasyMock.isA(ReviewData.class), EasyMock.eq("some patch"));
		EasyMock.expectLastCall().andThrow(new CrucibleException("test"));
		crucibleSessionMock.logout();

		replay(crucibleSessionMock);

		ServerBean server = prepareServerBean();
		ReviewData reviewData = prepareReviewData();

		String patch = "some patch";

		ReviewData ret;

		try {

			reviewData.setProjectKey(INVALID_PROJECT_KEY);
			// test call
			ret = facade.createReviewFromPatch(server, reviewData, patch);

			fail("creating review with patch with invalid key should throw an CrucibleException()");
		} catch (CrucibleException e) {
		} finally {
			EasyMock.verify(crucibleSessionMock);

		}
		;

	}

	private ReviewData prepareReviewData() {
		ReviewData reviewData = new ReviewData();
		reviewData.setAuthor(CxfReviewServiceMockImpl.VALID_LOGIN);
		reviewData.setCreator(CxfReviewServiceMockImpl.VALID_LOGIN);
		reviewData.setDescription("Test description");
		reviewData.setName("TEST");
		reviewData.setState(State.DRAFT);
		reviewData.setProjectKey("TEST");
		return reviewData;
	}

	private ServerBean prepareServerBean() {
		ServerBean server = new ServerBean();
		server.setUrlString(CxfReviewServiceMockImpl.VALID_URL);
		server.setUserName(CxfReviewServiceMockImpl.VALID_LOGIN);
		server.setPasswordString(CxfReviewServiceMockImpl.VALID_PASSWORD, false);
		return server;
	}

	public void _testCreateReviewHardcoded() throws ServerPasswordNotProvidedException {

		//facade.setCrucibleSession(null);

		ServerBean server = new ServerBean();
		server.setUrlString("http://lech.atlassian.pl:8060");
		server.setUserName("test");
		server.setPasswordString("test", false);

		ReviewData reviewData = new ReviewData();
		reviewData.setAuthor("test");
		reviewData.setCreator("test");
		reviewData.setDescription("XXX test description");
		reviewData.setName("XXX");
		reviewData.setState(State.DRAFT);
		reviewData.setProjectKey("TEST");

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

		List<ReviewData> list = null;

		try {
			list = facade.getAllReviews(server);
			assertNotNull(list);
			assertTrue(list.size() > 0);
		} catch (CrucibleException e) {
			fail(e.getMessage());
		}
	}
}
