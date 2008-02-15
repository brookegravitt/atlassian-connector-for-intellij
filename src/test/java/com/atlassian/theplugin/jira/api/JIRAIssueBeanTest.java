package com.atlassian.theplugin.jira.api;

import junit.framework.TestCase;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class JIRAIssueBeanTest extends TestCase {
    public void testFromXml() throws Exception
    {
        Document doc = new SAXBuilder().build(this.getClass().getResourceAsStream("/jira/api/single-issue.xml"));
        JIRAIssueBean issue = new JIRAIssueBean("http://jira.com", doc.getRootElement());
        assertEquals("NullPointerException on wrong URL to Bamboo server", issue.getSummary());
        assertEquals("PL-94", issue.getKey());
        assertEquals("http://jira.com", issue.getServerUrl());
        assertEquals("http://jira.com/browse/PL", issue.getProjectUrl());
        assertEquals("http://jira.com/browse/PL-94", issue.getIssueUrl());
    }
}
