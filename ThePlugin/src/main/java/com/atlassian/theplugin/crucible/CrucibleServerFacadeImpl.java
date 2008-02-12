package com.atlassian.theplugin.crucible;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.crucible.api.CrucibleException;
import com.atlassian.theplugin.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.crucible.api.CrucibleSession;
import com.atlassian.theplugin.crucible.api.CrucibleSessionImpl;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.RpcReviewServiceName;
import com.atlassian.theplugin.crucible.api.soap.xfire.review.ReviewData;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-05
 * Time: 16:27:35
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleServerFacadeImpl implements CrucibleServerFacade {
	private CrucibleSession crucibleSession = null;
	private static final Category LOG = Logger.getInstance(CrucibleServerFacadeImpl.class);
	private static final String SERVICE_REVIEW_SUFFIX = "service/review";

	public CrucibleServerFacadeImpl () {

	}

	/**
	 *
	 * @param serverUrl @see com.atlassian.theplugin.crucible.api.CrucibleSessionImpl#constructor(String baseUrl)
	 * @param userName
	 * @param password
	 * @throws CrucibleException 
	 */
	public void testServerConnection(String serverUrl, String userName, String password) throws CrucibleException {
		CrucibleSession session = crucibleSession;

		if (session == null) {
			session = new CrucibleSessionImpl(serverUrl);
		}
		
		session.login(userName, password);
		session.logout();
	}

	/**
	 * Creates new review in Crucible
	 * @param server 
	 * @param reviewData data for new review to create (some fields have to be set e.g. projectKey)
	 * @return created revew date
	 * @throws CrucibleException in case of createReview error or CrucibleLoginException in case of login error
	 */
	public ReviewData createReview(Server server, ReviewData reviewData) throws CrucibleException {
		CrucibleSession session = crucibleSession;

		if (session == null) {
			session = new CrucibleSessionImpl(server.getUrlString());
		}

		session.login(server.getUsername(), server.getPasswordString());

		RpcReviewServiceName crucibleService = createServiceProxy(server);

		ReviewData ret;

		try {
			ret = crucibleService.createReview(session.getAuthToken(), reviewData);
		} catch (RuntimeException e) {
			LOG.error(e.getMessage());
			throw new CrucibleException(e.getMessage(), e);
		} finally {
			session.logout();
		}

		return ret;
	}

	/**
	 * Creates new review in Crucible
	 * @param server
	 * @param reviewData data for new review to create (some fields have to be set e.g. projectKey)
	 * @param patch patch to assign with the review
	 * @return created revew date
	 * @throws CrucibleException in case of createReview error or CrucibleLoginException in case of login error
	 */
	public ReviewData createReviewFromPatch(ServerBean server, ReviewData reviewData, String patch) throws CrucibleException {
		CrucibleSession session = crucibleSession;

		if (session == null) {
			session = new CrucibleSessionImpl(server.getUrlString());
		}

		session.login(server.getUsername(), server.getPasswordString());

		RpcReviewServiceName crucibleService = createServiceProxy(server);

		ReviewData ret;

		try {
			ret = crucibleService.createReviewFromPatch(session.getAuthToken(), reviewData, patch);
		} catch (RuntimeException e) {
			LOG.error(e.getMessage());
			throw new CrucibleException(e.getMessage(), e);
		} finally {
			session.logout();
		}

		return ret;
	}

	/**
	 *
	 * @param server server object with Url, Login and Password to connect to 
	 * @return List of reviews (empty list in case there is no review)
	 */
	public List<Object> getAllReviews(Server server) throws CrucibleException {
		CrucibleSession session = crucibleSession;

		if (session == null) {
			session = new CrucibleSessionImpl(server.getUrlString());
		}

		session.login(server.getUsername(), server.getPasswordString());

		RpcReviewServiceName crucibleServiceProxy = createServiceProxy(server);

		List<Object> allReviews;

		try {
			allReviews = crucibleServiceProxy.getAllReviews(session.getAuthToken());
		} catch (RuntimeException e) {
			LOG.error(e.getMessage());
			throw new CrucibleException(e.getMessage(), e);
		}

		session.logout();

		return allReviews;
	}

	private RpcReviewServiceName createServiceProxy(Server server) {

		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(RpcReviewServiceName.class);
		factory.setAddress(formatUrl(server.getUrlString()));
		RpcReviewServiceName crucibleService = (RpcReviewServiceName) factory.create();
		
		return crucibleService;
	}

	private String formatUrl(String url) {

		if (url.endsWith("/")) {
			url = url + SERVICE_REVIEW_SUFFIX;
		} else {
			url = url + "/" + SERVICE_REVIEW_SUFFIX;
		}
		
		return url;
	}


	/**
	 * Used only for tests purposes. Should not be used manually but only for tests injections.
	 * @param crucibleSession
	 */
	public void setCrucibleSession(CrucibleSession crucibleSession) {
		this.crucibleSession = crucibleSession;
	}


}
