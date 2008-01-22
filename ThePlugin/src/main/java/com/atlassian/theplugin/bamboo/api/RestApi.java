package com.atlassian.theplugin.bamboo.api;

import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.atlassian.theplugin.bamboo.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-11
 * Time: 11:54:01
 * To change this template use File | Settings | File Templates.
 */
public class RestApi {
    private static final String LOGIN_ACTION = "/api/rest/login.action";
    private static final String LOGOUT_ACTION = "/api/rest/logout.action";
    private static final String LIST_PROJECT_ACTION = "/api/rest/listProjectNames.action";
    private static final String LIST_PLAN_ACTION = "/api/rest/listBuildNames.action";
    private static final String LATEST_BUILD_FOR_PLAN_ACTION = "/api/rest/getLatestBuildResults.action";
    private static final String LATEST_BUILDS_FOR_PROJECT_ACTION = "/api/rest/getLatestBuildResultsForProject.action";

    private String baseUrl;
    private String authToken;



    private RestApi(String baseUrl, String authToken) {
        this.baseUrl = baseUrl;
        this.authToken = authToken;
    }

    public static RestApi login(String url, String name, String password) throws BambooLoginException {
        String loginUrl = null;
        try {
            loginUrl = url + LOGIN_ACTION + "?username=" + URLEncoder.encode(name, "UTF-8") + "&password=" +
                    URLEncoder.encode(password, "UTF-8") + "&os_username=" +
                    URLEncoder.encode(name,"UTF-8") + "&os_password=" + URLEncoder.encode(password, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BambooLoginException("URLEncoding problem: " + e.getMessage());
        } catch (NullPointerException e) {
            throw new BambooLoginException("Corrupted configuration: " + e.getMessage());
        }

        Document doc = null;
        List elements = null;
        try {
            doc = retrieveResponse(loginUrl);
        } catch(BambooException e) {
            throw new BambooLoginException(e.getMessage(), e);
        }

        try {
            XPath xpath = XPath.newInstance("/response/auth");
            elements = xpath.selectNodes(doc);
            if (elements != null) {
                for (Object element : elements) {
                    Element e = (Element) element;
                    String authToken = e.getText();
                    return new RestApi(url, authToken);
                }
            }
        } catch (JDOMException e) {
            throw new BambooLoginException(e.getMessage(), e);
        }
             
        return null;
    }

    public void logout() throws BambooLoginException {
        String logoutUrl = null;
        try {
            logoutUrl = baseUrl + LOGOUT_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
            retrieveResponse(logoutUrl);
        } catch (UnsupportedEncodingException e) {
            throw new BambooLoginException("URLEncoding problem: " + e.getMessage());
        } catch (BambooException e) {
            throw new BambooLoginException("URLEncoding problem: " + e.getMessage());
       }
    }

    public List<BambooProject> listProjectNames() throws BambooException {
        String buildResultUrl = null;
        try {
            buildResultUrl = baseUrl + LIST_PROJECT_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BambooLoginException("URLEncoding problem: " + e.getMessage());
        }
        Document doc = retrieveResponse(buildResultUrl);

        XPath xpath = null;
        List projects = new ArrayList();
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
            throw new BambooLoginException("URLEncoding problem: " + e.getMessage());
        }
        Document doc = retrieveResponse(buildResultUrl);

        XPath xpath = null;
        List plans = new ArrayList();
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
        String buildResultUrl = null;
        try {
            buildResultUrl = baseUrl + LATEST_BUILD_FOR_PLAN_ACTION + "?auth=" + URLEncoder.encode(authToken, "UTF-8") + "&buildKey=" + URLEncoder.encode(planKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BambooLoginException("URLEncoding problem: " + e.getMessage());
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
            throw new BambooLoginException("URLEncoding problem: " + e.getMessage());
        }
        Document doc = retrieveResponse(buildResultUrl);

        XPath xpath = null;
        List builds = new ArrayList();
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
        String buildKey =getChildText(buildItemNode, "buildKey");
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

    private String getChildText(Element node, String childName){
        try {
            return node.getChild(childName).getText();
        } catch (Exception e){

            return "";
        }
    }

    private static Document retrieveResponse(String url) throws BambooException {
        try {
            URL callUrl = null;
            callUrl = new URL(url);
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(url);
            checkForErrors(doc);
            return doc;
        } catch(JDOMException e){
            throw new BambooException(e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw new BambooException(e.getMessage(), e);
        } catch(IOException e){
            throw new BambooException(e.getMessage(), e);
        }
    }

    private static void checkForErrors(Document doc) throws JDOMException, BambooException {
        XPath xpath = XPath.newInstance("/errors/error");
        List elements = xpath.selectNodes(doc);

        if (elements != null && elements.size() > 0) {
            String exceptionMsg = "";
            for(Iterator i = elements.iterator(); i.hasNext();) {
                Element e = (Element)  i.next();
                exceptionMsg += e.getText() + "\n";
            }
            throw new BambooException(exceptionMsg);
        }
    }
}
