package com.atlassian.theplugin.bamboo.api;

import com.atlassian.theplugin.bamboo.*;
import com.atlassian.theplugin.util.HttpClientFactory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Communication stub for Bamboo REST API.
 */
class BambooSessionImpl implements BambooSession {
	private static final String LOGIN_ACTION = "/api/rest/login.action";
	private static final String LOGOUT_ACTION = "/api/rest/logout.action";
	private static final String LIST_PROJECT_ACTION = "/api/rest/listProjectNames.action";
	private static final String LIST_PLAN_ACTION = "/api/rest/listBuildNames.action";
	private static final String LATEST_BUILD_FOR_PLAN_ACTION = "/api/rest/getLatestBuildResults.action";
	private static final String LATEST_USER_BUILDS_ACTION = "/api/rest/getLatestUserBuilds.action";
	private static final String GET_BUILD_DETAILS_ACTION = "/api/rest/getBuildResultsDetails.action";
	private static final String ADD_LABEL_ACTION = "/api/rest/addLabelToBuildResults.action";
	private static final String ADD_COMMENT_ACTION = "/api/rest/addCommentToBuildResults.action";

	private final String baseUrl;
	private String authToken;

	private HttpClient client = null;
	private static final int CONNECTION_TIMOUT = 20000;
	private static final String AUTHENTICATION_ERROR_MESSAGE = "User not authenticated yet, or session timed out";

	/**
	 * Public constructor for BambooSessionImpl.
	 *
	 * @param baseUrl base URL for Bamboo instance
	 */
	public BambooSessionImpl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/**
	 * Connects to Bamboo server instance. On successful login authentication token is returned from
	 * server and stored in Bamboo session for subsequent calls.
	 * <p/>
	 * The exception returned may have the getCause() examined for to get the actual exception reason.<br>
	 * If the exception is caused by a valid error response from the server (no IOEXception, UnknownHostException,
	 * MalformedURLException or JDOMException), the {@link com.atlassian.theplugin.bamboo.api.BambooLoginFailedException}
	 * is actually thrown. This may be used as a hint that the password is invalid.
	 *
	 * @param name	  username defined on Bamboo server instance
	 * @param aPassword for username
	 * @throws BambooLoginException on connection or authentication errors
	 */
	public void login(String name, char[] aPassword) throws BambooLoginException {
		String loginUrl;
		try {
			if (baseUrl == null) {
				throw new BambooLoginException("Corrupted configuration. Url null");
			}
			if ("".equals(baseUrl)) {
				throw new BambooLoginException("Corrupted configuration. Url empty");
			}
			if (name == null || aPassword == null) {
				throw new BambooLoginException("Corrupted configuration. Username or aPassword null");
			}
			String pass = String.valueOf(aPassword);
			loginUrl = baseUrl + LOGIN_ACTION + "?username=" + URLEncoder.encode(name, "UTF-8") + "&password="
					+ URLEncoder.encode(pass, "UTF-8") + "&os_username="
					+ URLEncoder.encode(name, "UTF-8") + "&os_password=" + URLEncoder.encode(pass, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("URLEncoding problem: " + e.getMessage());
		}

		try {
			Document doc = retrieveResponse(loginUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				throw new BambooLoginFailedException(exception);
			}
			XPath xpath = XPath.newInstance("/response/auth");
			List elements = xpath.selectNodes(doc);
			if (elements == null) {
				throw new BambooLoginException("Server did not return any authentication token");
			}
			if (elements.size() != 1) {
				throw new BambooLoginException("Server did returned excess authentication tokens (" + elements.size() + ")");
			}
			this.authToken = ((Element) elements.get(0)).getText();
		} catch (MalformedURLException e) {
			throw new BambooLoginException("Malformed server URL: " + baseUrl, e);
		} catch (UnknownHostException e) {
			throw new BambooLoginException("Unknown host: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new BambooLoginException(e.getMessage(), e);
		} catch (JDOMException e) {
			throw new BambooLoginException("Server returned malformed response", e);
		} catch (BambooSessionExpiredException e) {
			throw new BambooLoginException("Session expired", e);
		}
	}

	public void logout() {
		if (!isLoggedIn()) {
			return;
		}

		try {
			String logoutUrl = baseUrl + LOGOUT_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
			retrieveResponse(logoutUrl);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("URLEncoding problem", e);
		} catch (IOException e) {
			/* ignore errors on logout */
		} catch (JDOMException e) {
			/* ignore errors on logout */
		} catch (BambooSessionExpiredException e) {
			/* ignore errors on logout */
		}


		authToken = null;
		client = null;
	}

	public List<BambooProject> listProjectNames() throws BambooException {
		String buildResultUrl;
		try {
			buildResultUrl = baseUrl + LIST_PROJECT_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("URLEncoding problem: ", e);
		}

		List<BambooProject> projects = new ArrayList<BambooProject>();
		try {
			Document doc = retrieveResponse(buildResultUrl);
			XPath xpath = XPath.newInstance("/response/project");
			@SuppressWarnings("unchecked")
			List<Element> elements = xpath.selectNodes(doc);
			if (elements != null) {
				for (Element element : elements) {
					String name = element.getChild("name").getText();
					String key = element.getChild("key").getText();
					projects.add(new BambooProjectInfo(name, key));
				}
			}
		} catch (JDOMException e) {
			throw new BambooException("Server returned malformed response", e);
		} catch (IOException e) {
			throw new BambooException(e.getMessage(), e);
		}

		return projects;
	}

	public List<BambooPlan> listPlanNames() throws BambooException {
		String buildResultUrl;
		try {
			buildResultUrl = baseUrl + LIST_PLAN_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("URLEncoding problem: ", e);
		}

		List<BambooPlan> plans = new ArrayList<BambooPlan>();
		try {
			Document doc = retrieveResponse(buildResultUrl);
			XPath xpath = XPath.newInstance("/response/build");
			@SuppressWarnings("unchecked")
			List<Element> elements = xpath.selectNodes(doc);
			if (elements != null) {
				for (Element element : elements) {
					String enabledValue = element.getAttributeValue("enabled");
					boolean enabled = true;
					if (enabledValue != null) {
						enabled = Boolean.parseBoolean(enabledValue);
					}
					String name = element.getChild("name").getText();
					String key = element.getChild("key").getText();
					BambooPlanData plan = new BambooPlanData(name, key);
					plan.setEnabled(enabled);
					plans.add(plan);
				}
			}
		} catch (JDOMException e) {
			throw new BambooException("Server returned malformed response", e);
		} catch (IOException e) {
			throw new BambooException(e.getMessage(), e);
		}

		return plans;
	}

	/**
	 * Returns a {@link com.atlassian.theplugin.bamboo.BambooBuild} information about the latest build in a plan.
	 * <p/>
	 * Returned structure contains either the information about the build or an error message if the connection fails.
	 *
	 * @param planKey ID of the plan to get info about
	 * @return Information about the last build or error message
	 */
	public BambooBuild getLatestBuildForPlan(String planKey) throws BambooSessionExpiredException {
		String buildResultUrl;
		try {
			buildResultUrl = baseUrl + LATEST_BUILD_FOR_PLAN_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8")
					+ "&buildKey=" + URLEncoder.encode(planKey, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("URLEncoding problem: " + e.getMessage());
		}

		try {
			Document doc = retrieveResponse(buildResultUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				return constructBuildErrorInfo(planKey, exception, new Date());
			}

			XPath xpath = XPath.newInstance("/response");
			List elements = xpath.selectNodes(doc);
			if (elements != null && !elements.isEmpty()) {
				Element e = (Element) elements.iterator().next();
				return constructBuildItem(e, new Date());
			} else {
				return constructBuildErrorInfo(planKey, "Malformed server reply: no response element", new Date());
			}
		} catch (IOException e) {
			return constructBuildErrorInfo(planKey, e.getMessage(), new Date());
		} catch (JDOMException e) {
			return constructBuildErrorInfo(planKey, "Server returned malformed response", new Date());
		}
	}

	public List<String> getFavouriteUserPlans() throws BambooSessionExpiredException {
		List<String> builds = new ArrayList<String>();
		String buildResultUrl;
		try {
			buildResultUrl = baseUrl + LATEST_USER_BUILDS_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("URLEncoding problem: " + e.getMessage());
		}

		try {
			Document doc = retrieveResponse(buildResultUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				return builds;
			}

			XPath xpath = XPath.newInstance("/response/build");
			List elements = xpath.selectNodes(doc);
			if (elements != null) {
				for (Object element : elements) {
					Element e = (Element) element;
					builds.add(e.getChildText("key"));
				}
				return builds;
			} else {
				return builds;
			}
		} catch (IOException e) {
			return builds;
		} catch (JDOMException e) {
			return builds;
		}
	}

	public BuildDetails getBuildResultDetails(String buildKey, String buildNumber) throws BambooException {
		String buildResultUrl;
		try {
			buildResultUrl = baseUrl + GET_BUILD_DETAILS_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8")
					+ "&buildKey=" + URLEncoder.encode(buildKey, "UTF-8")
					+ "&buildNumber=" + URLEncoder.encode(buildNumber, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("URLEncoding problem: " + e.getMessage());
		}

		try {
			BuildDetailsInfo build = new BuildDetailsInfo();
			Document doc = retrieveResponse(buildResultUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				throw new BambooException(exception);
			}

			XPath xpath = XPath.newInstance("/response");
			List<Element> elements = xpath.selectNodes(doc);
			if (!elements.isEmpty()) {
				for (Element element : elements) {
					String vcsRevisionKey = element.getAttributeValue("vcsRevisionKey");
					if (vcsRevisionKey != null) {
						build.setVcsRevisionKey(vcsRevisionKey);
					}
				}
			}

			xpath = XPath.newInstance("/response/commits/commit");
			elements = xpath.selectNodes(doc);
			if (!elements.isEmpty()) {
				int i = 1;
				for (Element element : elements) {
					CommitInfo cInfo = new CommitInfo();
					cInfo.setAuthor(element.getAttributeValue("author"));
					cInfo.setCommitDate(parseCommitTime(element.getAttributeValue("date")));
					cInfo.setComment(getChildText(element, "comment"));

					String path = "/response/commits/commit[" + i++ + "]/files/file";
					XPath filesPath = XPath.newInstance(path);
					List<Element> fileElements = filesPath.selectNodes(doc);
					for (Element file : fileElements) {
						CommitFileInfo fileInfo = new CommitFileInfo();
						fileInfo.setFileName(file.getAttributeValue("name"));
						fileInfo.setRevision(file.getAttributeValue("revision"));
						cInfo.addCommitFile(fileInfo);
					}
					build.addCommitInfo(cInfo);
				}
			}

			xpath = XPath.newInstance("/response/successfulTests/testResult");
			elements = xpath.selectNodes(doc);
			if (!elements.isEmpty()) {
				for (Element element : elements) {
					TestDetailsInfo tInfo = new TestDetailsInfo();
					tInfo.setTestClassName(element.getAttributeValue("testClass"));
					tInfo.setTestMethodName(element.getAttributeValue("testMethod"));
					double duration = 0;
					try {
						duration = Double.valueOf(element.getAttributeValue("duration"));
					} catch (NumberFormatException e) {
						// leave 0
					}
					tInfo.setTestDuration(duration);
					tInfo.setTestResult(TestResult.TEST_SUCCEED);
					build.addSuccessfulTest(tInfo);
				}
			}

			xpath = XPath.newInstance("/response/failedTests/testResult");
			elements = xpath.selectNodes(doc);
			if (!elements.isEmpty()) {
				int i = 1;
				for (Element element : elements) {
					TestDetailsInfo tInfo = new TestDetailsInfo();
					tInfo.setTestClassName(element.getAttributeValue("testClass"));
					tInfo.setTestMethodName(element.getAttributeValue("testMethod"));
					double duration = 0;
					try {
						duration = Double.valueOf(element.getAttributeValue("duration"));
					} catch (NumberFormatException e) {
						// leave 0
					}
					tInfo.setTestDuration(duration);
					tInfo.setTestResult(TestResult.TEST_FAILED);

					String path = "/response/failedTests/testResult[" + i++ + "]/errors/error";
					XPath errorPath = XPath.newInstance(path);
					List<Element> errorElements = errorPath.selectNodes(doc);
					for (Element error : errorElements) {
						tInfo.setTestErrors(error.getText());
					}
					build.addFailedTest(tInfo);
				}
			}

			return build;
		} catch (JDOMException e) {
			throw new BambooException("Server returned malformed response", e);
		} catch (IOException e) {
			throw new BambooException(e.getMessage(), e);
		}
	}

	public void addLabelToBuild(String buildKey, String buildNumber, String buildLabel) throws BambooException {
		String buildResultUrl;
		try {
			buildResultUrl = baseUrl + ADD_LABEL_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8")
					+ "&buildKey=" + URLEncoder.encode(buildKey, "UTF-8")
					+ "&buildNumber=" + URLEncoder.encode(buildNumber, "UTF-8")
					+ "&label=" + URLEncoder.encode(buildLabel, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("URLEncoding problem: ", e);
		}

		try {
			Document doc = retrieveResponse(buildResultUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				throw new BambooException(exception);
			}
		} catch (JDOMException e) {
			throw new BambooException("Server returned malformed response", e);
		} catch (IOException e) {
			throw new BambooException(e.getMessage(), e);
		}
	}

	public void addCommentToBuild(String buildKey, String buildNumber, String buildComment) throws BambooException {
		String buildResultUrl;
		try {
			buildResultUrl = baseUrl + ADD_COMMENT_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8")
					+ "&buildKey=" + URLEncoder.encode(buildKey, "UTF-8")
					+ "&buildNumber=" + URLEncoder.encode(buildNumber, "UTF-8")
					+ "&content=" + URLEncoder.encode(buildComment, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("URLEncoding problem: ", e);
		}

		try {
			Document doc = retrieveResponse(buildResultUrl);
			String exception = getExceptionMessages(doc);
			if (null != exception) {
				throw new BambooException(exception);
			}
		} catch (JDOMException e) {
			throw new BambooException("Server returned malformed response", e);
		} catch (IOException e) {
			throw new BambooException(e.getMessage(), e);
		}
	}

	BambooBuild constructBuildErrorInfo(String planId, String message, Date lastPollingTime) {
		BambooBuildInfo buildInfo = new BambooBuildInfo();

		buildInfo.setServerUrl(baseUrl);
		buildInfo.setBuildKey(planId);
		buildInfo.setBuildState(BuildStatus.UNKNOWN.toString());
		buildInfo.setMessage(message);
		buildInfo.setPollingTime(lastPollingTime);

		return buildInfo;
	}

	private BambooBuildInfo constructBuildItem(Element buildItemNode, Date lastPollingTime) {
		BambooBuildInfo buildInfo = new BambooBuildInfo();

		buildInfo.setServerUrl(baseUrl);

		buildInfo.setProjectName(getChildText(buildItemNode, "projectName"));
		buildInfo.setBuildName(getChildText(buildItemNode, "buildName"));
		buildInfo.setBuildKey(getChildText(buildItemNode, "buildKey"));
		buildInfo.setBuildState(getChildText(buildItemNode, "buildState"));
		buildInfo.setBuildNumber(getChildText(buildItemNode, "buildNumber"));
		buildInfo.setBuildReason(getChildText(buildItemNode, "buildReason"));
		buildInfo.setBuildDurationDescription(getChildText(buildItemNode, "buildDurationDescription"));
		buildInfo.setBuildTestSummary(getChildText(buildItemNode, "buildTestSummary"));
		buildInfo.setBuildCommitComment(getChildText(buildItemNode, "buildCommitComment"));
		buildInfo.setBuildRelativeBuildDate(getChildText(buildItemNode, "buildRelativeBuildDate"));

		buildInfo.setBuildTime(parseBuildTime(getChildText(buildItemNode, "buildTime")));
		buildInfo.setPollingTime(lastPollingTime);

		return buildInfo;
	}

	private SimpleDateFormat buildTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Date parseBuildTime(String date) {
		try {
			return buildTimeFormat.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	private SimpleDateFormat commitTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	private Date parseCommitTime(String date) {
		try {
			return commitTimeFormat.parse(date);
		} catch (ParseException e) {
			return null;
		}
	}

	private String getChildText(Element node, String childName) {
		try {
			return node.getChild(childName).getText();
		} catch (Exception e) {
			return "";
		}
	}

	private Document retrieveResponse(String urlString) throws IOException, JDOMException, BambooSessionExpiredException {
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

		client.getHttpConnectionManager().getParams().setConnectionTimeout(CONNECTION_TIMOUT);
		client.executeMethod(method);

		if (method.getStatusCode() != HttpStatus.SC_OK) {
			throw new IOException(method.getStatusText());
		}

		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(method.getResponseBodyAsStream());

		method.releaseConnection();

		String error = getExceptionMessages(doc);
		if (error != null) {
			if (error.startsWith(AUTHENTICATION_ERROR_MESSAGE)) {
				throw new BambooSessionExpiredException("Session expired.");
			}
		}

		return doc;
	}

	private static String getExceptionMessages(Document doc) throws JDOMException {
		XPath xpath = XPath.newInstance("/errors/error");
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
