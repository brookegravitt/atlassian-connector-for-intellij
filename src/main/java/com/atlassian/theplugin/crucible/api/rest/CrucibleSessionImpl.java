package com.atlassian.theplugin.crucible.api.rest;

import com.atlassian.theplugin.crucible.api.*;
import com.atlassian.theplugin.rest.*;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import thirdparty.net.iharder.base64.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Communication stub for Bamboo REST API.
 */
public class CrucibleSessionImpl extends AbstractRestSession implements CrucibleSession  {
	private static final String AUTH_SERVICE = "/rest-service/auth-v1";
	private static final String REVIEW_SERVICE = "/rest-service/reviews-v1";
	private static final String PROJECTS_SERVICE = "/rest-service/projects-v1";
	private static final String REPOSITORIES_SERVICE = "/rest-service/repositories-v1";
	private static final String LOGIN = "/login";
	private static final String GET_REVIEWS_IN_STATES = "?state=";
	private static final String GET_REVIEWERS = "/reviewers";

	private String authToken = null;

	/**
	 * Public constructor for BambooSessionImpl.
	 *
	 * @param baseUrl base URL for Bamboo instance
	 */
	public CrucibleSessionImpl(String baseUrl) throws RestException {
		super(baseUrl);
	}

	public void login(String username, String aPassword) throws RestLoginException {
		if (!isLoggedIn()) {
			String loginUrl;
			try {
				if (baseUrl == null) {
					throw new RestLoginException("Corrupted configuration. Url null");
				}
				if ("".equals(baseUrl.trim())) {
					throw new RestLoginException("Corrupted configuration. Url empty");
				}
				if (username == null || aPassword == null) {
					throw new RestLoginException("Corrupted configuration. Username or aPassword null");
				}
				loginUrl = baseUrl + AUTH_SERVICE + LOGIN + "?userName=" + URLEncoder.encode(username, "UTF-8")
						+ "&password=" + URLEncoder.encode(aPassword, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				///CLOVER:OFF
				throw new RuntimeException("URLEncoding problem: " + e.getMessage());
				///CLOVER:ON
			}

			try {
				Document doc = retrieveGetResponse(loginUrl);
				String exception = getExceptionMessages(doc);
				if (null != exception) {
					throw new RestLoginFailedException(exception);
				}
				XPath xpath = XPath.newInstance("/loginResult/token");
				List elements = xpath.selectNodes(doc);
				if (elements == null) {
					throw new RestLoginException("Server did not return any authentication token");
				}
				if (elements.size() != 1) {
					throw new RestLoginException("Server did returned excess authentication tokens ("
							+ elements.size() + ")");
				}
				this.authToken = ((Element) elements.get(0)).getText();
				this.userName = username;
				this.password = aPassword;
			} catch (MalformedURLException e) {
				throw new RestLoginException("Malformed server URL: " + baseUrl, e);
			} catch (UnknownHostException e) {
				throw new RestLoginException("Unknown host: " + e.getMessage(), e);
			} catch (IOException e) {
				throw new RestLoginException(e.getMessage(), e);
			} catch (JDOMException e) {
				throw new RestLoginException("Server returned malformed response", e);
			} catch (RestSessionExpiredException e) {
				// Crucible does not return this exception
			}
		}
	}

	public void logout() {
		if (authToken != null) {
			authToken = null;
			userName = null;
			password = null;
		}
	}

	public List<ReviewData> getReviewsInStates(List<State> states) throws RestException {
		if (!isLoggedIn()) {
			throw new IllegalStateException("Calling method without calling login() first");
		}

		StringBuilder sb = new StringBuilder();
		sb.append(baseUrl);
		sb.append(REVIEW_SERVICE);
		if (states != null && states.size() != 0) {
			sb.append(GET_REVIEWS_IN_STATES);
			for (Iterator<State> stateIterator = states.iterator(); stateIterator.hasNext();) {
				State state = stateIterator.next();
				sb.append(state.value());
				if (stateIterator.hasNext()) {
					sb.append(",");
				}
			}
		}

		try {
			Document doc = retrieveGetResponse(sb.toString());

			XPath xpath = XPath.newInstance("/reviews/reviewData");
			@SuppressWarnings("unchecked")
			List<Element> elements = xpath.selectNodes(doc);
			List<ReviewData> reviews = new ArrayList<ReviewData>();

			if (elements != null && !elements.isEmpty()) {
				for (Element element : elements) {
					reviews.add(CrucibleRestXmlHelper.parseReviewNode(element));
				}
			}
			return reviews;
		} catch (IOException e) {
			throw new RestException(e.getMessage(), e);
		} catch (JDOMException e) {
			throw new RestException("Server returned malformed response", e);
		}
	}

	public List<ReviewData> getAllReviews() throws RestException {
		return getReviewsInStates(null);
	}

	public List<String> getReviewers(PermId permId) throws RestException {
		if (!isLoggedIn()) {
			throw new IllegalStateException("Calling method without calling login() first");
		}

		String requestUrl = baseUrl + REVIEW_SERVICE + "/" + permId.getId() + GET_REVIEWERS;
		try {
			Document doc = retrieveGetResponse(requestUrl);

			XPath xpath = XPath.newInstance("/reviewers/reviewer");
			@SuppressWarnings("unchecked")
			List<Element> elements = xpath.selectNodes(doc);
			List<String> reviewers = new ArrayList<String>();

			if (elements != null && !elements.isEmpty()) {
				for (Element element : elements) {
					reviewers.add(element.getText());
				}
			}
			return reviewers;
		} catch (IOException e) {
			throw new RestException(e.getMessage(), e);
		} catch (JDOMException e) {
			throw new RestException("Server returned malformed response", e);
		}
	}

	public List<ProjectData> getProjects() throws RestException {
		if (!isLoggedIn()) {
			throw new IllegalStateException("Calling method without calling login() first");
		}

		String requestUrl = baseUrl + PROJECTS_SERVICE;
		try {
			Document doc = retrieveGetResponse(requestUrl);

			XPath xpath = XPath.newInstance("/projects/projectData");
			@SuppressWarnings("unchecked")
			List<Element> elements = xpath.selectNodes(doc);
			List<ProjectData> projects = new ArrayList<ProjectData>();

			if (elements != null && !elements.isEmpty()) {
				for (Element element : elements) {
					projects.add(CrucibleRestXmlHelper.parseProjectNode(element));
				}
			}
			return projects;
		} catch (IOException e) {
			throw new RestException(e.getMessage(), e);
		} catch (JDOMException e) {
			throw new RestException("Server returned malformed response", e);
		}
	}

	public List<RepositoryData> getRepositories() throws RestException {
		if (!isLoggedIn()) {
			throw new IllegalStateException("Calling method without calling login() first");
		}

		String requestUrl = baseUrl + REPOSITORIES_SERVICE;
		try {
			Document doc = retrieveGetResponse(requestUrl);

			XPath xpath = XPath.newInstance("/repositories/repoData");
			@SuppressWarnings("unchecked")
			List<Element> elements = xpath.selectNodes(doc);
			List<RepositoryData> repositories = new ArrayList<RepositoryData>();

			if (elements != null && !elements.isEmpty()) {
				for (Element element : elements) {
					repositories.add(CrucibleRestXmlHelper.parseRepositoryNode(element));
				}
			}
			return repositories;
		} catch (IOException e) {
			throw new RestException(e.getMessage(), e);
		} catch (JDOMException e) {
			throw new RestException("Server returned malformed response", e);
		}
	}

	public ReviewData createReview(ReviewData reviewData) throws RestException {
		if (!isLoggedIn()) {
			throw new IllegalStateException("Calling method without calling login() first");
		}
		return createReviewFromPatch(reviewData, null);
	}

	public ReviewData createReviewFromPatch(ReviewData review, String patch) throws RestException {
		if (!isLoggedIn()) {
			throw new IllegalStateException("Calling method without calling login() first");
		}

		Document request = CrucibleRestXmlHelper.prepareCreateReviewNode(review, patch);

		try {
			Document doc = retrievePostResponse(baseUrl + REVIEW_SERVICE, request);

			XPath xpath = XPath.newInstance("/reviewData");
			@SuppressWarnings("unchecked")
			List<Element> elements = xpath.selectNodes(doc);

			if (elements != null && !elements.isEmpty()) {
				return CrucibleRestXmlHelper.parseReviewNode(elements.iterator().next());
			}
			return null;
		} catch (IOException e) {
			throw new RestException(e.getMessage(), e);
		} catch (JDOMException e) {
			throw new RestException("Server returned malformed response", e);
		}
	}

	protected void adjustHttpHeader(HttpMethod method) {
		method.addRequestHeader(new Header("Authorization", getAuthHeaderValue()));
	}

	protected void preprocessResult(Document doc) throws JDOMException, RestSessionExpiredException {

	}

	private String getAuthHeaderValue() {
		return "Basic " + encode(userName + ":" + password);
	}

	private synchronized String encode(String str2encode) {
		try {
			return Base64.encodeBytes(str2encode.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 is not supported", e);
		}
	}

	private static String getExceptionMessages(Document doc) throws JDOMException {
		XPath xpath = XPath.newInstance("/loginResult/error");
		@SuppressWarnings("unchecked")
		List<Element> elements = xpath.selectNodes(doc);

		if (elements != null && elements.size() > 0) {
			StringBuffer exceptionMsg = new StringBuffer();
			for (Element e : elements) {
				exceptionMsg.append(e.getText());
				exceptionMsg.append("\n");
			}
			return exceptionMsg.toString();
		} else {
			/* no exception */
			return null;
		}
	}

	public boolean isLoggedIn() {
		return authToken != null;
	}
}