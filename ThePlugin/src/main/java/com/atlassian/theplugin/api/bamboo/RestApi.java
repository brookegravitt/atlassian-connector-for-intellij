package com.atlassian.theplugin.api.bamboo;

import org.jdom.input.SAXBuilder;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;

import com.atlassian.theplugin.bamboo.BambooBuildInfo;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-01-11
 * Time: 11:54:01
 * To change this template use File | Settings | File Templates.
 */
public class RestApi {
    private String baseUrl;
    private String authToken;

    private RestApi(String baseUrl, String authToken) {
        this.baseUrl = baseUrl;
        this.authToken = authToken;
    }

    public static RestApi login(String url, String name, String password) throws BambooLoginException {
        SAXBuilder builder = new SAXBuilder();

        URL loginUrl = null;
        try {
            loginUrl = new URL(url + "/api/rest/login.action?username=" + name + "&password=" + password + "&os_username=" + name + "&os_password=" + password);
            // TODO encode URL
        } catch (MalformedURLException e) {
            throw new BambooLoginException(e.getMessage(), e.getCause());
        }

        List elements = null;
        try {
            Document doc = builder.build(loginUrl);
            checkForLoginErrors(doc);
            XPath xpath = XPath.newInstance("/response/auth");
            elements = xpath.selectNodes(doc);
            if (elements != null) {
                for (Object element : elements) {
                    Element e = (Element) element;
                    String authToken = e.getText();
                    return new RestApi(url, authToken);
                }
            }
        }catch(JDOMException e){
            throw new BambooLoginException(e.getMessage(), e);
        }catch(IOException e){
            throw new BambooLoginException(e.getMessage(), e);
        }
        return null;
    }

    public void logout() throws BambooException {
        String logoutUrl = baseUrl + "/api/rest/logout.action?auth=" + authToken;
        retrieveResponse(logoutUrl);
    }

    public List listProjectNames() throws BambooException {
        String buildResultUrl = baseUrl + "/api/rest//api/rest/listProjectNames.action?auth=" + authToken;
        Document doc = retrieveResponse(buildResultUrl);

        XPath xpath = null;
        List projects = new ArrayList();
        List elements = null;
        try {
            xpath = XPath.newInstance("/response/build");
            elements = xpath.selectNodes(doc);
            if (elements != null) {
                for (Object element : elements) {
                    Element e = (Element) element;
                    projects.add(constructBuildItem(e, false));
                }
            }
        } catch (JDOMException e) {
            throw new BambooException(e.getMessage(), e);
        }

        return projects;
    }

    public List getLatestProjectBuilds(String projectKey) throws BambooException {
        String buildResultUrl = baseUrl + "/api/rest/getLatestBuildResultst.action?auth=" + authToken + "&projectKey=" + projectKey;
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
                    builds.add(constructBuildItem(e, false));
                }
            }
        } catch (JDOMException e) {
            throw new BambooException(e.getMessage(), e);
        }

        return builds;
    }

    private static BambooBuildInfo constructBuildItem(Element buildItemNode, boolean isAuthorTriggeredBuilds)
    {
        String projectName = "";//buildItemNode.getChild("projectName").getText();
        String buildName = "";//buildItemNode.getChild("buildName").getText();
        String buildKey = buildItemNode.getChild("buildKey").getText();
        String buildState = buildItemNode.getChild("buildState").getText().toUpperCase();
        String buildNumber = buildItemNode.getChild("buildNumber").getText();
        String buildReason = "";//buildItemNode.getChild("buildReason").getText();
        String buildRelativeBuildDate = "";//buildItemNode.getChild("buildRelativeBuildDate").getText();
        String buildDurationDescription = "";//buildItemNode.getChild("buildDurationDescription").getText();
        String buildTestSummary = "";//buildItemNode.getChild("buildTestSummary").getText();

        if(isAuthorTriggeredBuilds)
        {
            String buildCommitComment = "";//buildItemNode.getChild("buildCommitComment").getText();
            return new BambooBuildInfo(projectName, buildName, buildKey, buildState, buildNumber, buildReason, buildRelativeBuildDate,
                buildDurationDescription, buildTestSummary, buildCommitComment);
        }

        return new BambooBuildInfo(projectName, buildName, buildKey, buildState, buildNumber, buildReason, buildRelativeBuildDate,
            buildDurationDescription, buildTestSummary);
    }
    
    private Document retrieveResponse(String url) throws BambooException {
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
            throw new BambooException(e.getMessage(), e.getCause());
        } catch(IOException e){
            throw new BambooException(e.getMessage(), e);
        }
    }


    private static void checkForLoginErrors(Document doc) throws JDOMException, BambooLoginException {
        XPath xpath = XPath.newInstance("/errors/error");
        List elements = xpath.selectNodes(doc);
        if (elements != null && elements.size() > 0) {
            Element e = (Element) elements.iterator().next();
            throw new BambooLoginException(e.getText());
        }
    }

    private static void checkForErrors(Document doc) throws JDOMException, BambooException {
        XPath xpath = XPath.newInstance("/errors/error");
        List elements = xpath.selectNodes(doc);
        if (elements != null && elements.size() > 0) {
            Element e = (Element) elements.iterator().next();
            throw new BambooException(e.getText());
        }
    }
}
