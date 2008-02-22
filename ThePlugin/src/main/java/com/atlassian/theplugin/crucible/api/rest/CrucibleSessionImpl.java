package com.atlassian.theplugin.crucible.api.rest;

import com.atlassian.theplugin.crucible.api.*;
import com.atlassian.theplugin.util.HttpClientFactory;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import thirdparty.net.iharder.base64.Base64;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Communication stub for Bamboo REST API.
 */
public class CrucibleSessionImpl implements CrucibleSession {
	private static final String AUTH_SERVICE = "/rest-service/auth-v1";
	private static final String REVIEW_SERVICE = "/rest-service/reviews-v1";
	private static final String LOGIN = "/login";
	private static final String GET_REVIEWS_IN_STATES = "?state=";
	private static final String GET_REVIEWERS = "/reviewers";

	private final String baseUrl;
	private String userName;
	private String password;
	private HttpClient client = null;
	private String authToken = null;


	/**
	 * Public constructor for BambooSession.
	 *
	 * @param baseUrl base URL for Bamboo instance
	 */
	public CrucibleSessionImpl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void login(String username, String aPassword) throws CrucibleLoginException {
		if (!isLoggedIn()) {
			String loginUrl;
			try {
				if (baseUrl == null) {
					throw new CrucibleLoginException("Corrupted configuration. Url null");
				}
				if ("".equals(baseUrl)) {
					throw new CrucibleLoginException("Corrupted configuration. Url empty");
				}
				if (username == null || aPassword == null) {
					throw new CrucibleLoginException("Corrupted configuration. Username or aPassword null");
				}
				loginUrl = baseUrl + AUTH_SERVICE + LOGIN + "?userName=" + URLEncoder.encode(username, "UTF-8") +
						"&password=" + URLEncoder.encode(aPassword, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("URLEncoding problem: " + e.getMessage());
			}

			try {
				Document doc = retrieveGetResponse(loginUrl);
				String exception = getExceptionMessages(doc);
				if (null != exception) {
					throw new CrucibleLoginFailedException(exception);
				}
				XPath xpath = XPath.newInstance("/loginResult/token");
				List elements = xpath.selectNodes(doc);
				if (elements == null) {
					throw new CrucibleLoginException("Server did not return any authentication token");
				}
				if (elements.size() != 1) {
					throw new CrucibleLoginException("Server did returned excess authentication tokens (" + elements.size() + ")");
				}
				this.authToken = ((Element) elements.get(0)).getText();
				this.userName = username;
				this.password = aPassword;
			} catch (MalformedURLException e) {
				throw new CrucibleLoginException("Malformed server URL: " + baseUrl, e);
			} catch (UnknownHostException e) {
				throw new CrucibleLoginException("Unknown host: " + e.getMessage(), e);
			} catch (IOException e) {
				throw new CrucibleLoginException(e.getMessage(), e);
			} catch (JDOMException e) {
				throw new CrucibleLoginException("Server returned malformed response", e);
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

	public List<ReviewData> getReviewsInStates(List<State> states) throws CrucibleException {
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
			List<Element> elements = xpath.selectNodes(doc);
			List<ReviewData> reviews = new ArrayList<ReviewData>();

			if (elements != null && !elements.isEmpty()) {
				for (Element element : elements) {
					reviews.add(constructReviewData(element));
				}
			}
			return reviews;
		} catch (IOException e) {
			throw new CrucibleException(e.getMessage(), e);
		} catch (JDOMException e) {
			throw new CrucibleException("Server returned malformed response", e);
		}
	}

	public List<ReviewData> getAllReviews() throws CrucibleException {
		return getReviewsInStates(null);
	}

	public List<String> getReviewers(PermId permId) throws CrucibleException {
		if (!isLoggedIn()) {
			throw new IllegalStateException("Calling method without calling login() first");
		}

		String requestUrl = baseUrl + REVIEW_SERVICE + "/" + permId.getId() + GET_REVIEWERS;
		try {
			Document doc = retrieveGetResponse(requestUrl);

			XPath xpath = XPath.newInstance("/reviewers/reviewer");
			List<Element> elements = xpath.selectNodes(doc);
			List<String> reviewers = new ArrayList<String>();

			if (elements != null && !elements.isEmpty()) {
				for (Element element : elements) {
					reviewers.add(element.getText());
				}
			}
			return reviewers;
		} catch (IOException e) {
			throw new CrucibleException(e.getMessage(), e);
		} catch (JDOMException e) {
			throw new CrucibleException("Server returned malformed response", e);
		}
	}

	public ReviewData createReview(ReviewData reviewData) throws CrucibleException {
		if (!isLoggedIn()) {
			throw new IllegalStateException("Calling method without calling login() first");
		}
		return createReviewFromPatch(reviewData, null);
	}

	public ReviewData createReviewFromPatch(ReviewData review, String patch) throws CrucibleException {
		if (!isLoggedIn()) {
			throw new IllegalStateException("Calling method without calling login() first");
		}

		Document request = prepareCreateReviewFromPatch(review, patch);

		try {
			Document doc = retrievePostResponse(baseUrl + REVIEW_SERVICE, request);

			XPath xpath = XPath.newInstance("/reviewData");
			List<Element> elements = xpath.selectNodes(doc);

			if (elements != null && !elements.isEmpty()) {
				return constructReviewData(elements.iterator().next());
			}
			return null;
		} catch (IOException e) {
			throw new CrucibleException(e.getMessage(), e);
		} catch (JDOMException e) {
			throw new CrucibleException("Server returned malformed response", e);
		}
	}

	private ReviewData constructReviewData(Element reviewNode) {
		ReviewDataBean review = new ReviewDataBean();

		review.setAuthor(getChildText(reviewNode, "author"));
		review.setCreator(getChildText(reviewNode, "creator"));
		review.setModerator(getChildText(reviewNode, "moderator"));
		review.setDescription(getChildText(reviewNode, "description"));
		review.setName(getChildText(reviewNode, "name"));
		review.setProjectKey(getChildText(reviewNode, "projectKey"));
		review.setRepoName(getChildText(reviewNode, "repoName"));

		String stateString = getChildText(reviewNode, "state");
		if (!"".equals(stateString)) {
			review.setState(State.fromValue(stateString));
		}

		if (reviewNode.getChild("permaId") != null) {
			PermIdBean permId = new PermIdBean();
			permId.setId(reviewNode.getChild("permaId").getChild("id").getText());
			review.setPermaId(permId);
		}

		return review;
	}

	void addTag(Element root, String tagName, String tagValue) {
		Element newElement = new Element(tagName);
		newElement.addContent(tagValue);
		root.addContent(newElement);
	}

	private Document prepareCreateReviewFromPatch(ReviewData review, String patch) {
		Element root = new Element("createReview");
		Document doc = new Document(root);
		Element reviewData = new Element("reviewData");
		root.setContent(reviewData);

		addTag(reviewData, "author", review.getAuthor());
		addTag(reviewData, "creator", review.getCreator());
		addTag(reviewData, "description", review.getDescription());
		addTag(reviewData, "moderator", review.getModerator());
		addTag(reviewData, "name", review.getName());
		addTag(reviewData, "projectKey", review.getProjectKey());
		addTag(reviewData, "repoName", review.getRepoName());

		if (patch != null) {
			Element patchData = new Element("patch");
			root.addContent(patchData);

			CDATA patchT = new CDATA(patch);
			patchData.setContent(patchT);
		}

		return doc;
	}

	private String getChildText(Element node, String childName) {
		try {
			return node.getChild(childName).getText();
		} catch (Exception e) {
			return "";
		}
	}

	private Document retrieveGetResponse(String urlString) throws IOException, JDOMException {
		// validate URL first
		try {
			URL url = new URL(urlString);
			// check the host name
			if (url.getHost().length() == 0) {
				throw new MalformedURLException("Url must contain valid host.");
			}
		} catch (MalformedURLException e) {
			throw new MalformedURLException("Url must contain valid host.");
		}

		if (client == null) {
			client = HttpClientFactory.getClient();
		}

		GetMethod method = new GetMethod(urlString);
		method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);

		method.addRequestHeader(new Header("Authorization", getAuthHeaderValue()));

		client.executeMethod(method);

		if (method.getStatusCode() != HttpStatus.SC_OK) {
			throw new IOException(method.getStatusText());
		}

		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(method.getResponseBodyAsStream());

		method.releaseConnection();

		return doc;
	}

	private Document retrievePostResponse(String urlString, Document request) throws IOException, JDOMException {
		// validate URL first
		try {
			URL url = new URL(urlString);
			// check the host name
			if (url.getHost().length() == 0) {
				throw new MalformedURLException("Url must contain valid host.");
			}
		} catch (MalformedURLException e) {
			throw new MalformedURLException("Url must contain valid host.");
		}

		if (client == null) {
			client = HttpClientFactory.getClient();
		}

		PostMethod method = new PostMethod(urlString);
		method.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
		method.addRequestHeader(new Header("Authorization", getAuthHeaderValue()));

		XMLOutputter serializer = new XMLOutputter(Format.getCompactFormat());
		method.setRequestEntity(new StringRequestEntity(serializer.outputString(request), "application/xml", "UTF-8"));

		client.executeMethod(method);

		if (method.getStatusCode() != HttpStatus.SC_OK) {
			throw new IOException(method.getStatusText());
		}

		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(method.getResponseBodyAsStream());

		method.releaseConnection();

		return doc;
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