package com.atlassian.theplugin.bamboo.api;

import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.util.List;
import java.util.ArrayList;

import com.atlassian.theplugin.bamboo.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-11
 * Time: 11:54:01
 * To change this template use File | Settings | File Templates.
 */
public class BambooSession {
    public static final String LOGIN_ACTION = "/api/rest/login.action";
    private static final String LOGOUT_ACTION = "/api/rest/logout.action";
    private static final String LIST_PROJECT_ACTION = "/api/rest/listProjectNames.action";
    private static final String LIST_PLAN_ACTION = "/api/rest/listBuildNames.action";
    private static final String LATEST_BUILD_FOR_PLAN_ACTION = "/api/rest/getLatestBuildResults.action";
    private static final String LATEST_BUILDS_FOR_PROJECT_ACTION = "/api/rest/getLatestBuildResultsForProject.action";

    private String baseUrl;
    private String userName;
    private char[] password;
    private String authToken;

    public BambooSession(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void login(String name, char[] password) throws BambooLoginException {
        String loginUrl;
        try {
            if (baseUrl == null) {
                throw new BambooLoginException("Corrupted configuration. Url null");
            }
            if (name == null || password == null) {
                throw new BambooLoginException("Corrupted configuration. Username or password null");
            }
            String pass = String.valueOf(password);
            loginUrl = baseUrl + LOGIN_ACTION + "?username=" + URLEncoder.encode(name, "UTF-8") + "&password=" +
                    URLEncoder.encode(pass, "UTF-8") + "&os_username=" +
                    URLEncoder.encode(name, "UTF-8") + "&os_password=" + URLEncoder.encode(pass, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: " + e.getMessage());
        }

        Document doc;
        List elements;
        try {
            doc = retrieveResponse(loginUrl);
        } catch (BambooException e) {
            throw new BambooLoginException(e.getMessage(), e);
        }

        try {
            XPath xpath = XPath.newInstance("/response/auth");
            elements = xpath.selectNodes(doc);
            if (elements != null) {
                for (Object element : elements) {
                    Element e = (Element) element;
                    this.authToken = e.getText();
                }
            }
        } catch (JDOMException e) {
            throw new BambooLoginException(e.getMessage(), e);
        }
    }

    public void logout() {
        if (!isLoggedIn()) {
            return;
        }

        String logoutUrl = null;
        try {
            logoutUrl = baseUrl + LOGOUT_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
            retrieveResponse(logoutUrl);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: " + e.getMessage());
        } catch (BambooException e) {
            /* ignore error on logout */
        }

        authToken = null;
    }

    public List<BambooProject> listProjectNames() throws BambooException {
        String buildResultUrl = null;
        try {
            buildResultUrl = baseUrl + LIST_PROJECT_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: " + e.getMessage());
        }
        Document doc = retrieveResponse(buildResultUrl);

        XPath xpath = null;
        List<BambooProject> projects = new ArrayList<BambooProject>();
        List elements = null;
        try {
            xpath = XPath.newInstance("/response/project");
            elements = xpath.selectNodes(doc);
            if (elements != null) {
                for (Object element : elements) {
                    Element e = (Element) element;
                    String name = e.getChild("name").getText();
                    String key = e.getChild("key").getText();
                    projects.add(new BambooProjectInfo(name, key));
                }
            }
        } catch (JDOMException e) {
            throw new BambooException(e.getMessage(), e);
        }

        return projects;
    }

    public List<BambooPlan> listPlanNames() throws BambooException {
        String buildResultUrl = null;
        try {
            buildResultUrl = baseUrl + LIST_PLAN_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: " + e.getMessage());
        }
        Document doc = retrieveResponse(buildResultUrl);

        XPath xpath = null;
        List<BambooPlan> plans = new ArrayList<BambooPlan>();
        List elements = null;
        try {
            xpath = XPath.newInstance("/response/build");
            elements = xpath.selectNodes(doc);
            if (elements != null) {
                for (Object element : elements) {
                    Element e = (Element) element;
                    String name = e.getChild("name").getText();
                    String key = e.getChild("key").getText();
                    plans.add(new BambooPlanInfo(name, key));
                }
            }
        } catch (JDOMException e) {
            throw new BambooException(e.getMessage(), e);
        }

        return plans;
    }

    public BambooBuildInfo getLatestBuildForPlan(String planKey) throws BambooException {
        String buildResultUrl;
        try {
            buildResultUrl = baseUrl + LATEST_BUILD_FOR_PLAN_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8") + "&buildKey=" + URLEncoder.encode(planKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: " + e.getMessage());
        }
        Document doc = retrieveResponse(buildResultUrl);

        XPath xpath = null;
        List elements = null;
        try {
            xpath = XPath.newInstance("/response");
            elements = xpath.selectNodes(doc);
            if (elements != null && !elements.isEmpty()) {
                Element e = (Element) elements.iterator().next();
                return constructBuildItem(e);
            }
        } catch (JDOMException e) {
            throw new BambooException(e.getMessage(), e);
        }

        return null;
    }

    public List<BambooBuild> getLatestBuildsForProject(String projectKey) throws BambooException {
        String buildResultUrl = null;
        try {
            buildResultUrl = baseUrl + LATEST_BUILDS_FOR_PROJECT_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8") + "&projectKey=" + URLEncoder.encode(projectKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URLEncoding problem: " + e.getMessage());
        }
        Document doc = retrieveResponse(buildResultUrl);

        XPath xpath = null;
        List<BambooBuild> builds = new ArrayList<BambooBuild>();
        List elements = null;
        try {
            xpath = XPath.newInstance("/response/build");
            elements = xpath.selectNodes(doc);
            if (elements != null) {
                for (Object element : elements) {
                    Element e = (Element) element;
                    builds.add(constructBuildItem(e));
                }
            }
        } catch (JDOMException e) {
            throw new BambooException(e.getMessage(), e);
        }

        return builds;
    }

    private BambooBuildInfo constructBuildItem(Element buildItemNode) {
        String projectName = getChildText(buildItemNode, "projectName");
        String buildName = getChildText(buildItemNode, "buildName");
        String buildKey = getChildText(buildItemNode, "buildKey");
        String buildState = getChildText(buildItemNode, "buildState");
        String buildNumber = getChildText(buildItemNode, "buildNumber");
        String buildReason = getChildText(buildItemNode, "buildReason");
        String buildRelativeBuildDate = getChildText(buildItemNode, "buildRelativeBuildDate");
        String buildDurationDescription = getChildText(buildItemNode, "buildDurationDescription");
        String buildTestSummary = getChildText(buildItemNode, "buildTestSummary");
        String buildCommitComment = getChildText(buildItemNode, "buildCommitComment");

        return new BambooBuildInfo(projectName, buildName, buildKey, buildState, buildNumber, buildReason, buildRelativeBuildDate,
                buildDurationDescription, buildTestSummary, buildCommitComment);
    }

    private String getChildText(Element node, String childName) {
        try {
            return node.getChild(childName).getText();
        } catch (Exception e) {
            return "";
        }
    }

    private Document retrieveResponse(String urlString) throws BambooException {
        try {
            URLConnection c = HttpConnectionFactory.getConnection(urlString);
            InputStream is = c.getInputStream();

            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(is);
            checkForErrors(doc);
            return doc;
        } catch (JDOMException e) {
            throw new BambooException(e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw new BambooException(e.getMessage(), e);
        } catch (IOException e) {
            throw new BambooException(e.getMessage(), e);
        }
    }

    private static void checkForErrors(Document doc) throws JDOMException, BambooException {
        XPath xpath = XPath.newInstance("/errors/error");
        List<Element> elements = xpath.selectNodes(doc);

        if (elements != null && elements.size() > 0) {
            String exceptionMsg = "";
            for (Element e : elements) {
                exceptionMsg += e.getText() + "\n";
            }
            throw new BambooException(exceptionMsg);
        }
    }

    public boolean isLoggedIn() {
        return authToken != null;
    }
}
