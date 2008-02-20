package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.auth.RpcAuthServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.RpcReviewServiceName;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import java.util.ArrayList;
import java.util.List;


public class CrucibleSessionImpl implements CrucibleSession {
	private String crucibleAuthUrl;
	private String crucibleReviewUrl;


	private String authToken;
	private RpcAuthServiceName authService;
	private RpcReviewServiceName reviewService;

	private static final String SERVICE_AUTH_SUFFIX = "service/auth";
	private static final String SERVICE_REVIEW_SUFFIX = "service/reviewtmp";

	/**
	 * @param baseUrl url to the Crucible installation (without /service/auth suffix)
	 */
	public CrucibleSessionImpl(String baseUrl) {
		crucibleAuthUrl = baseUrl;

		if (baseUrl.endsWith("/")) {
			crucibleAuthUrl = baseUrl + SERVICE_AUTH_SUFFIX;
			crucibleReviewUrl = baseUrl + SERVICE_REVIEW_SUFFIX;
		} else {
			crucibleAuthUrl = baseUrl + "/" + SERVICE_AUTH_SUFFIX;
			crucibleReviewUrl = baseUrl + "/" + SERVICE_REVIEW_SUFFIX;
		}

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		Thread.currentThread().setContextClassLoader(factory.getClass().getClassLoader());
		factory.setServiceClass(RpcAuthServiceName.class);
		factory.setAddress(crucibleAuthUrl);
		authService = (RpcAuthServiceName) factory.create();

		JaxWsProxyFactoryBean reviewFactory = new JaxWsProxyFactoryBean();
		reviewFactory.setServiceClass(RpcReviewServiceName.class);
		reviewFactory.setAddress(crucibleReviewUrl);
		reviewService = (RpcReviewServiceName) reviewFactory.create();
	}

	public void login(String userName, String password) throws CrucibleLoginException {
		if (authToken != null) {
			throw new IllegalStateException("Calling login on already logged in session.");
		}
		try {
			authToken = authService.login(userName, password);
		} catch (RuntimeException e) {
			throw new CrucibleLoginException("Login failed", e);
		}

		if (authToken == null || getAuthToken().length() == 0) {
			authToken = null; // nullify when empty
			throw new CrucibleLoginException("Login failed");
		}
	}

	public void logout() {
		if (authToken != null) {
			authService.logout(authToken);
		}
		authToken = null;
	}

	public ReviewData createReview(ReviewData reviewData) throws CrucibleException {
		String token = getAuthToken();
		try {
			return translateFromCxfReviewData(reviewService.createReview(token, translateToCxfReviewData(reviewData)));
		} catch (RuntimeException e) {
			throw new CrucibleException("createReview", e);
		}
	}

	public ReviewData createReviewFromPatch(ReviewData reviewData, String patch) throws CrucibleException {
		String token = getAuthToken();
		try {
			return translateFromCxfReviewData(
					reviewService.createReviewFromPatch(token, translateToCxfReviewData(reviewData), patch));
		} catch (RuntimeException e) {
			throw new CrucibleException("createReviewFromPatch", e);
		}

	}

	public List<ReviewData> getReviewsInStates(List<State> arg1) throws CrucibleException {
		String token = getAuthToken();
		try {
			List<com.atlassian.theplugin.crucible.api.soap.xfire.review.State> states =
					new ArrayList<com.atlassian.theplugin.crucible.api.soap.xfire.review.State>();
			for (State state : arg1) {
				states.add(translateToCxfState(state));
			}
			List<com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData> result =
					reviewService.getReviewsInStates(token, states);
			List<ReviewData> r = new ArrayList<ReviewData>();
			for (com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData review : result) {
				r.add(translateFromCxfReviewData(review));
			}
			return r;
		} catch (RuntimeException e) {
			throw new CrucibleException("getReviewInStates", e);
		}
	}

	public List<ReviewData> getAllReviews() throws CrucibleException {
		String token = getAuthToken();
		try {
			List<com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData> result =
					reviewService.getAllReviews(token);
			List<ReviewData> r = new ArrayList<ReviewData>();
			for (com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData review : result) {
				r.add(translateFromCxfReviewData(review));
			}
			return r;
		} catch (RuntimeException e) {
			throw new CrucibleException("getAllReviews", e);
		}
	}

	public List<String> getReviewers(PermId arg1) throws CrucibleException {
		String token = getAuthToken();
		try {
			return reviewService.getReviewers(token, translateToCxfPermId(arg1));
		} catch (RuntimeException e) {
			throw new CrucibleException("getReviewers", e);
		}
	}

	private String getAuthToken() {
		if (authToken == null) {
			throw new IllegalStateException("Calling method without calling login() first");
		}
		return authToken;
	}

	static com.atlassian.theplugin.crucible.api.soap.xfire.review.State translateToCxfState(State state) {
		switch (state) {
			case APPROVAL:
				return com.atlassian.theplugin.crucible.api.soap.xfire.review.State.APPROVAL;
			case CLOSED:
				return com.atlassian.theplugin.crucible.api.soap.xfire.review.State.CLOSED;
			case DEAD:
				return com.atlassian.theplugin.crucible.api.soap.xfire.review.State.DEAD;
			case DRAFT:
				return com.atlassian.theplugin.crucible.api.soap.xfire.review.State.DRAFT;
			case REJECTED:
				return com.atlassian.theplugin.crucible.api.soap.xfire.review.State.REJECTED;
			case REVIEW:
				return com.atlassian.theplugin.crucible.api.soap.xfire.review.State.REVIEW;
			case SUMMARIZE:
				return com.atlassian.theplugin.crucible.api.soap.xfire.review.State.SUMMARIZE;
			case UNKNOWN:
			default:
				return com.atlassian.theplugin.crucible.api.soap.xfire.review.State.UNKNOWN;
		}
	}

	static State translateFromCxfState(com.atlassian.theplugin.crucible.api.soap.xfire.review.State state) {
		switch (state) {
			case APPROVAL:
				return State.APPROVAL;
			case CLOSED:
				return State.CLOSED;
			case DEAD:
				return State.DEAD;
			case DRAFT:
				return State.DRAFT;
			case REJECTED:
				return State.REJECTED;
			case REVIEW:
				return State.REVIEW;
			case SUMMARIZE:
				return State.SUMMARIZE;
			case UNKNOWN:
			default:
				return State.UNKNOWN;
		}
	}

	static com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId translateToCxfPermId(PermId permId) {
		if (permId == null) {
			return null;
		}
		com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId newPerm =
				new com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId();
		newPerm.setId(permId.getId());
		return newPerm;
	}

	static
	com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData translateToCxfReviewData(ReviewData reviewData) {
		com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData newReview =
				new com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData();
		newReview.setPermaId(translateToCxfPermId(reviewData.getPermaId()));
		newReview.setAuthor(reviewData.getAuthor());
		newReview.setCreator(reviewData.getCreator());
		newReview.setDescription(reviewData.getDescription());
		newReview.setModerator(reviewData.getModerator());
		newReview.setName(reviewData.getName());
		newReview.setParentReview(translateToCxfPermId(reviewData.getParentReview()));
		newReview.setProjectKey(reviewData.getProjectKey());
		newReview.setRepoName(reviewData.getRepoName());
		newReview.setState(translateToCxfState(reviewData.getState()));
		return newReview;
	}

	static
	ReviewData translateFromCxfReviewData(com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData reviewData) {
		ReviewDataBean newReview = new ReviewDataBean();
		newReview.setPermaId(translateToCxfPermId(reviewData.getPermaId()));
		newReview.setAuthor(reviewData.getAuthor());
		newReview.setCreator(reviewData.getCreator());
		newReview.setDescription(reviewData.getDescription());
		newReview.setModerator(reviewData.getModerator());
		newReview.setName(reviewData.getName());
		newReview.setParentReview(translateToCxfPermId(reviewData.getParentReview()));
		newReview.setProjectKey(reviewData.getProjectKey());
		newReview.setRepoName(reviewData.getRepoName());
		newReview.setState(translateFromCxfState(reviewData.getState()));
		return newReview;
	}	
}
