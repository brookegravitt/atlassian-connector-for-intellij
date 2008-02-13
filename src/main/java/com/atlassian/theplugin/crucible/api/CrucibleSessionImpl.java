package com.atlassian.theplugin.crucible.api;

import com.atlassian.theplugin.crucible.api.soap.xfire.auth.RpcAuthServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.RpcReviewServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.State;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.PermId;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.bus.CXFBusFactory;

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
    RpcAuthServiceName authService;
	RpcReviewServiceName reviewService;

	private static final String SERVICE_AUTH_SUFFIX = "service/auth";
	private static final String SERVICE_REVIEW_SUFFIX = "service/reviewtmp";

	/**
	 *
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
//        factory.setBus(CXFBusFactory.newInstance(CXFBusFactory.DEFAULT_BUS_FACTORY).createBus());
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
			throw new CrucibleLoginException("Login failed");
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

	public String getAuthToken() {
		return authToken;
	}

    public List<ReviewData> getReviewsInStates(List<State> arg1) {
		return reviewService.getReviewsInStates(authToken, arg1);
    }

    public List<ReviewData> getAllReviews() {
        return reviewService.getAllReviews(authToken);
    }

    public List<String> getReviewers(PermId arg1) {
        return reviewService.getReviewers(authToken, arg1);
    }
}
