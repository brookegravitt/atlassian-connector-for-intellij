package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.auth.RpcAuthServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.State;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;
import junit.framework.TestCase;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.mortbay.jetty.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 14:56:43
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleSessionTest extends TestCase {

	private Server httpServer;

	protected void setUp() throws Exception {

		CxfAuthServiceMockImpl authServiceMock = new CxfAuthServiceMockImpl();

		JaxWsServerFactoryBean serverFactory = new JaxWsServerFactoryBean();
        serverFactory.setServiceClass(RpcAuthServiceName.class);
		serverFactory.setAddress(CxfAuthServiceMockImpl.VALID_URL + "/service/auth");
		serverFactory.setServiceBean(authServiceMock);
		serverFactory.create();
	}

	protected void tearDown() throws Exception {
	}

	public void testSuccessCrucibleLogin() {

        CrucibleSessionImpl crucibleSession = null;
        crucibleSession = new CrucibleSessionImpl(CxfAuthServiceMockImpl.VALID_URL);

        try {
			crucibleSession.login(CxfAuthServiceMockImpl.VALID_LOGIN, CxfAuthServiceMockImpl.VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("Login failed while expected success: " + e.getMessage());
		}
	}

	public void testFailedCrucibleLogin() {
        CrucibleSessionImpl crucibleSession = null;
        crucibleSession = new CrucibleSessionImpl(CxfAuthServiceMockImpl.VALID_URL);

        try {
			crucibleSession.login(CxfAuthServiceMockImpl.INVALID_LOGIN, CxfAuthServiceMockImpl.INVALID_PASSWORD);
			fail("Login succeeded while expected failure.");
		} catch (CrucibleLoginException e) {

		}
	}

	public void testGetReviewsInStates() throws Exception {
//		CrucibleSession session = new CrucibleSessionImpl("http://lech.atlassian.pl:8060");
		CrucibleSession session = new CrucibleSessionImpl("http://192.168.157.245:8060");
		session.login("mwent", "d0n0tch@nge");

		List<State> states = new ArrayList<State>();
		states.add(State.REVIEW);

        List<ReviewData> reviews = session.getReviewsInStates(states);
        System.out.println("reviews.size() = " + reviews.size());
        
        for (Iterator<ReviewData> iterator = reviews.iterator(); iterator.hasNext();) {
            ReviewData reviewData = iterator.next();
            System.out.println("reviewData.getPermaId() = " + reviewData.getPermaId().getId());
            System.out.println("reviewData.getProjectKey() = " + reviewData.getProjectKey());
            System.out.println("reviewData.getAuthor() = " + reviewData.getAuthor());
            System.out.println("reviewData.getState() = " + reviewData.getState());
            List<String> reviewers = session.getReviewers(reviewData.getPermaId());
            for (Iterator<String> stringIterator = reviewers.iterator(); stringIterator.hasNext();) {
                String reviewer = stringIterator.next();
                System.out.println("reviewer = " + reviewer);
            }
        }
    }

	private void xtestCxf() throws CrucibleException {
		CrucibleSessionImpl session = null;

		session = new CrucibleSessionImpl("http://lech.atlassian.pl:8060");

		String userName = "test";
		String password = "test";

		try {
			session.login(userName, password);
		} catch (CrucibleLoginException e) {
			fail("login failed: " + e.getMessage());
		}

		session.logout();
	}
}
