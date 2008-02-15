package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.auth.RpcAuthServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.RpcReviewServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.State;
import junit.framework.TestCase;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

import java.util.ArrayList;
import java.util.List;

public class CrucibleSessionTest extends TestCase {
	private CxfAuthServiceMockImpl authServiceMock;
	private CxfReviewServiceMockImpl reviewServiceMock;

	protected void setUp() throws Exception {

		authServiceMock = new CxfAuthServiceMockImpl();

		JaxWsServerFactoryBean serverFactory = new JaxWsServerFactoryBean();
        serverFactory.setServiceClass(RpcAuthServiceName.class);
		serverFactory.setAddress(CxfAuthServiceMockImpl.VALID_URL + "/service/auth");
		serverFactory.setServiceBean(authServiceMock);
		serverFactory.create();

		reviewServiceMock = new CxfReviewServiceMockImpl();

		JaxWsServerFactoryBean reviewServerFactory = new JaxWsServerFactoryBean();
        reviewServerFactory.setServiceClass(RpcReviewServiceName.class);
		reviewServerFactory.setAddress(CxfReviewServiceMockImpl.VALID_URL + "/service/review");
		reviewServerFactory.setServiceBean(reviewServiceMock);
		reviewServerFactory.create();
	}

	protected void tearDown() throws Exception {

	}

	public void testSuccessCrucibleLogin() {

        CrucibleSessionImpl crucibleSession = new CrucibleSessionImpl(CxfAuthServiceMockImpl.VALID_URL);

        try {
			crucibleSession.login(CxfAuthServiceMockImpl.VALID_LOGIN, CxfAuthServiceMockImpl.VALID_PASSWORD);
		} catch (CrucibleLoginException e) {
			fail("Login failed while expected success: " + e.getMessage());
		}
	}

	public void testFailedCrucibleLogin() {
        CrucibleSessionImpl crucibleSession =  new CrucibleSessionImpl(CxfAuthServiceMockImpl.VALID_URL);

        try {
			crucibleSession.login(CxfAuthServiceMockImpl.INVALID_LOGIN, CxfAuthServiceMockImpl.INVALID_PASSWORD);
			fail("Login succeeded while expected failure.");
		} catch (CrucibleLoginException e) {

		}
	}

	public void testGetReviewsInStates() throws Exception {

		CrucibleSessionImpl crucibleSession = new CrucibleSessionImpl(CxfReviewServiceMockImpl.VALID_URL);

		List<State> states = new ArrayList<State>();
		states.add(State.REVIEW);

        List<ReviewData> reviews = crucibleSession.getReviewsInStates(states);
        
        for (ReviewData reviewData : reviews) {
            List<String> reviewers = crucibleSession.getReviewers(reviewData.getPermaId());
            for (String reviewer : reviewers) {
				assertNotNull(reviewer);
			}
        }

    }
}
