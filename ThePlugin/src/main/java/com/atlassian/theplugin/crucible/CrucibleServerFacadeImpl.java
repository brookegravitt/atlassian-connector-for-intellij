package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.crucible.api.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrucibleServerFacadeImpl implements CrucibleServerFacade {
	private Map<String, CrucibleSession> sessions = new HashMap<String, CrucibleSession>();

	public CrucibleServerFacadeImpl() {
	}

	private CrucibleSession getSession(String serverUrl) {
		CrucibleSession session = sessions.get(serverUrl);
		if (session == null) {
			session = new CrucibleSessionImpl(serverUrl);
			sessions.put(serverUrl, session);
		}
		return session;
	}

	/**
	 * @param serverUrl @see com.atlassian.theplugin.crucible.api.CrucibleSessionImpl#constructor(String baseUrl)
	 * @param userName
	 * @param password
	 * @throws CrucibleException
	 */
	public void testServerConnection(String serverUrl, String userName, String password) throws CrucibleException {
		CrucibleSession session = getSession(serverUrl);
		session.login(userName, password);
		session.logout();
	}

	/**
	 * Creates new review in Crucible
	 *
	 * @param server
	 * @param reviewData data for new review to create (some fields have to be set e.g. projectKey)
	 * @return created revew date
	 * @throws CrucibleException in case of createReview error or CrucibleLoginException in case of login error
	 */
	public ReviewData createReview(Server server, ReviewData reviewData) throws CrucibleException {
		CrucibleSession session = getSession(server.getUrlString());

		session.login(server.getUserName(), server.getPasswordString());

		try {
			return session.createReview(reviewData);
		} finally {
			session.logout();
		}
	}

	/**
	 * Creates new review in Crucible
	 *
	 * @param server
	 * @param reviewData data for new review to create (some fields have to be set e.g. projectKey)
	 * @param patch	  patch to assign with the review
	 * @return created revew date
	 * @throws CrucibleException in case of createReview error or CrucibleLoginException in case of login error
	 */
	public ReviewData createReviewFromPatch(ServerBean server, ReviewData reviewData, String patch) throws CrucibleException {
		CrucibleSession session = getSession(server.getUrlString());

		session.login(server.getUserName(), server.getPasswordString());

		try {
			return session.createReviewFromPatch(reviewData, patch);
		} finally {
			session.logout();
		}
	}

	/**
	 * @param server server object with Url, Login and Password to connect to
	 * @return List of reviews (empty list in case there is no review)
	 */
	public List<ReviewDataInfo> getAllReviews(Server server) throws CrucibleException {
		CrucibleSession session = getSession(server.getUrlString());

		session.login(server.getUserName(), server.getPasswordString());

		try {
			List<ReviewData> res = session.getAllReviews();
			List<ReviewDataInfo> result = new ArrayList<ReviewDataInfo>(res.size());
			for (ReviewData review : res) {
				List<String> reviewers = session.getReviewers(review.getPermaId());
				result.add(new ReviewDataInfoImpl(review, reviewers, server));
			}
			return result;
		} finally {
			session.logout();
		}
	}

	public List<ReviewDataInfo> getActiveReviewsForUser(Server server)
			throws CrucibleException {
		CrucibleSession session = getSession(server.getUrlString());

		session.login(server.getUserName(), server.getPasswordString());

		List<State> states = new ArrayList<State>();
		states.add(State.REVIEW);
		try {
			List<ReviewData> reviews = session.getReviewsInStates(states);
			List<ReviewDataInfo> result = new ArrayList<ReviewDataInfo>(reviews.size());

			for (ReviewData reviewData : reviews) {
				List<String> reviewers = session.getReviewers(reviewData.getPermaId());

				if (reviewers.contains(server.getUserName())) {
					result.add(new ReviewDataInfoImpl(reviewData, reviewers, server));
				}
			}
			return result;
		} finally {
			session.logout();
		}
	}

}
