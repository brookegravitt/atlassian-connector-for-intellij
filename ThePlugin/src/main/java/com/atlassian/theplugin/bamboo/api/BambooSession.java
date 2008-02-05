package com.atlassian.theplugin.bamboo.api;

import com.atlassian.theplugin.bamboo.*;
import com.atlassian.theplugin.util.HttpConnectionFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-11
 * Time: 11:54:01
 * To change this template use File | Settings | File Templates.
 */
public class BambooSession {
	private static final String LOGIN_ACTION = "/api/rest/login.action";
	private static final String LOGOUT_ACTION = "/api/rest/logout.action";
	private static final String LIST_PROJECT_ACTION = "/api/rest/listProjectNames.action";
	private static final String LIST_PLAN_ACTION = "/api/rest/listBuildNames.action";
	private static final String LATEST_BUILD_FOR_PLAN_ACTION = "/api/rest/getLatestBuildResults.action";
	private static final String LATEST_BUILDS_FOR_PROJECT_ACTION = "/api/rest/getLatestBuildResultsForProject.action";
	private static final String LATEST_USER_BUILDS_ACTION = "/api/rest/getLatestUserBuilds.action";	

	private final String baseUrl;
	//	private String userName;
	//	private char[] password;
	private String authToken;

	/**
	 * Public constructor for BambooSession.
	 *
	 * @param baseUrl base URL for Bamboo instance
	 */
	public BambooSession(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/**
	 * Connects to Bamboo server instance. On successful login authentication token is returned from
	 * server and stored in Bamboo session for subsequent calls
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
				throw new BambooLoginException("Login exception: " + exception);
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
		} catch (IOException e) {
			throw new BambooLoginException("IOException during login", e);
		} catch (JDOMException e) {
			throw new BambooLoginException("JDOMException during login", e);
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
		} catch (Exception e) {
			/* ignore errors on logout */
		}

		authToken = null;
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
			List<Element> elements = xpath.selectNodes(doc);
			if (elements != null) {
				for (Element element : elements) {
					String name = element.getChild("name").getText();
					String key = element.getChild("key").getText();
					projects.add(new BambooProjectInfo(name, key));
				}
			}
		} catch (JDOMException e) {
			throw new BambooException("JDOMException getting project names", e);
		} catch (IOException e) {
			throw new BambooException("IOException getting project names", e);
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
			List<Element> elements = xpath.selectNodes(doc);
			if (elements != null) {
				for (Element element : elements) {
					String name = element.getChild("name").getText();
					String key = element.getChild("key").getText();
					plans.add(new BambooPlanData(name, key));
				}
			}
		} catch (JDOMException e) {
			throw new BambooException("JDOMException getting plan names", e);
		} catch (IOException e) {
			throw new BambooException("IOException getting plan names", e);
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
	public BambooBuild getLatestBuildForPlan(String planKey) {
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
			return constructBuildErrorInfo(planKey, "IOException accessing the server" + e.getMessage(), new Date());
		} catch (JDOMException e) {
			return constructBuildErrorInfo(planKey, "JDOMException parsing the response: " + e.getMessage(), new Date());
		}
	}

	public List<String> getFavouriteUserPlans() {
		List<String> builds = new ArrayList<String>();
		String buildResultUrl;
		Date lastPoolingTime = new Date();
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



//  commented because nobody actually uses this method, and the unit test does not really test anything, so we
//	don't even know if the method works

//	public List<BambooBuild> getLatestBuildsForProject(String projectKey) throws BambooException {
//		String buildResultUrl;
//		Date lastPoolingTime = new Date();
//		try {
//			buildResultUrl = baseUrl + LATEST_BUILDS_FOR_PROJECT_ACTION + "?auth="
//					+ URLEncoder.encode(authToken, "UTF-8") + "&projectKey=" + URLEncoder.encode(projectKey, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			throw new RuntimeException("URLEncoding problem: " + e.getMessage());
//		}
//
//		Document doc = retrieveResponse(buildResultUrl);
//		List<BambooBuild> builds = new ArrayList<BambooBuild>();
//		try {
//			XPath xpath = XPath.newInstance("/response/build");
//			List elements = xpath.selectNodes(doc);
//			if (elements != null) {
//				for (Object element : elements) {
//					Element e = (Element) element;
//					builds.add(constructBuildItem(e, lastPoolingTime));
//				}
//			}
//		} catch (JDOMException e) {
//			throw new BambooException(e);
//		}
//
//		return builds;

	//	}

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

	private String getChildText(Element node, String childName) {
		try {
			return node.getChild(childName).getText();
		} catch (Exception e) {
			return "";
		}
	}

	private Document retrieveResponse(String urlString) throws IOException, JDOMException {
		URLConnection c = HttpConnectionFactory.getConnection(urlString);
		InputStream is = c.getInputStream();

		SAXBuilder builder = new SAXBuilder();
		return builder.build(is);
	}

	private static String getExceptionMessages(Document doc) throws JDOMException {
		XPath xpath = XPath.newInstance("/errors/error");
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
