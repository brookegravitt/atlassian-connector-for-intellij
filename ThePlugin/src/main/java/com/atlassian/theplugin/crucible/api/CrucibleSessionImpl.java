package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.auth.RpcAuthServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.RpcReviewServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.State;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import javax.xml.ws.soap.SOAPFaultException;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 15:10:39
 * To change this template use File | Settings | File Templates.
 */
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

		try {
			authToken = authService.login(userName, password);
		} catch (SOAPFaultException e) {
			throw new CrucibleLoginException("Login failed", e);
		}

		if (authToken == null || authToken.length() == 0) {
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
		try {
			return reviewService.createReview(authToken, reviewData);
		} catch (RuntimeException e) {
			throw new CrucibleException("createReview", e);
		}
	}

	public ReviewData createReviewFromPatch(ReviewData reviewData, String patch) throws CrucibleException {
		try {
			return reviewService.createReviewFromPatch(authToken, reviewData, patch);
		} catch (RuntimeException e) {
			throw new CrucibleException("createReviewFromPatch", e);
		}

	}

	public List<ReviewData> getReviewsInStates(List<State> arg1) throws CrucibleException {
		try {
			return reviewService.getReviewsInStates(authToken, arg1);
		} catch (RuntimeException e) {
			throw new CrucibleException("getReviewInStates", e);
		}
	}

	public List<ReviewData> getAllReviews() throws CrucibleException {
		try {
			return reviewService.getAllReviews(authToken);
		} catch (RuntimeException e) {
			throw new CrucibleException("getAllReviews", e);
		}

	}

	public List<String> getReviewers(PermId arg1) throws CrucibleException {
		try {
			return reviewService.getReviewers(authToken, arg1);
		} catch (RuntimeException e) {
			throw new CrucibleException("getReviewers", e);
		}
	}
}
