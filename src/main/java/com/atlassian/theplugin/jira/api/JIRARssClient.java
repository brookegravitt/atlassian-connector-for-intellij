/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 13/03/2004
 * Time: 23:19:19
 */
package com.atlassian.theplugin.jira.api;

import com.intellij.openapi.diagnostic.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class JIRARssClient {
    private static final Logger LOGGER = Logger.getInstance(JIRARssClient.class.getName());

    private String serverUrl;
    private String userName;
    private String password;

    public JIRARssClient(String url) {
        this.serverUrl = url;
    }

    public JIRARssClient(String url, String userName, String password) {
        this.serverUrl = url;
        this.userName = userName;
        this.password = password;
    }

    public List getAssignedIssues(String assignee) throws JIRAException {
        String url = serverUrl + "/sr/jira.issueviews:searchrequest-xml" +
                "/temp/SearchRequest.xml?resolution=-1&assignee=" + URLEncoder.encode(assignee) + "&sorter/field=updated&sorter/order=DESC&tempMax=100" + appendAuthentication();
//        System.out.println("url = " + url);
        try {
            Document doc = buildFeed(url);
            Element root = doc.getRootElement();
            Element channel = root.getChild("channel");
            if (channel != null && !channel.getChildren("item").isEmpty()) {
                return makeIssues(channel.getChildren("item"));
            }

            return Collections.EMPTY_LIST;
        } catch (Exception e) {
            throw new JIRAException(e.getMessage(), e);
        }
    }

    private List makeIssues(List issueElements) {
        List<JIRAIssue> result = new ArrayList<JIRAIssue>(issueElements.size());
        for (Iterator iterator = issueElements.iterator(); iterator.hasNext();) {
            result.add(new JIRAIssueBean(serverUrl, (Element) iterator.next()));
        }
        return result;
    }

    private String appendAuthentication() {
        if (userName != null) {
            return "&os_username=" + URLEncoder.encode(userName) +
                    "&os_password=" + URLEncoder.encode(password);
        }
        return "";
    }

    protected Document buildFeed(String url) throws IOException, JDOMException {

        // LOGGER.info("Refeshing issues from: " + url); // be careful about logging as we might leak the user's password - nasty!
        String feedContent = getUrlContent(url);
        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(false);
        return builder.build(new StringReader(feedContent));
    }

    protected String getUrlContent(String url) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(getUrlAsStream(url)));

        String inputLine;
        StringBuffer buffer = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            buffer.append(inputLine);
            buffer.append('\n');
        }

        in.close();

        return buffer.toString();
    }

    // protected so that we can easily write tests by simply returning XML from a file instead of a URL!
    protected InputStream getUrlAsStream(String url) throws IOException {
        return new URL(url).openConnection().getInputStream();
    }
}