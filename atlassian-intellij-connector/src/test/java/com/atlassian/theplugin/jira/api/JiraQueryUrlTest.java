package com.atlassian.theplugin.jira.api;

import com.atlassian.theplugin.commons.jira.api.JIRAPriorityBean;
import com.atlassian.theplugin.commons.jira.api.JIRAQueryFragment;
import com.atlassian.theplugin.commons.jira.api.JIRASavedFilterBean;
import com.atlassian.theplugin.commons.jira.api.JiraQueryUrl;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pmaruszak
 */
public class JiraQueryUrlTest extends TestCase {
    List<JIRAQueryFragment> queryFragment = new ArrayList<JIRAQueryFragment>();
    private static String SERVER_URL = "http://loft.spartez.com";
    @Override
    protected void setUp() throws Exception {
        queryFragment.add(new JIRAPriorityBean(1L,1,"priority", null));
        super.setUp();
    }

    public void testBuildIssueNavigatorUrl() {
        JiraQueryUrl queryUrl = new JiraQueryUrl.Builder().serverUrl(SERVER_URL)
                                                          .queryFragments(queryFragment)
                                                          .max(100)
                                                          .sortBy("priority")
                                                          .sortOrder("DESC")
                                                          .start(1)
                                                          .userName("myUserName")
                                                          .build();

        assertTrue(queryUrl.buildIssueNavigatorUrl()
                .equals("http://loft.spartez.com/secure/IssueNavigator.jspa?refreshFilter=false&"
                + "reset=update&show=View+%3E%3E&priority=1&sorter/field=priority&sorter/order=DESC&"
                + "pager/start=1&tempMax=100&os_authType=basic"));

    }

    public void testBuildRssSearchUrlUrl() {
         JiraQueryUrl queryUrl = new JiraQueryUrl.Builder().serverUrl(SERVER_URL)
                                                           .queryFragments(queryFragment)
                                                           .max(100)
                                                           .sortBy("priority")
                                                           .sortOrder("DESC")
                                                           .start(1)
                                                           .userName("myUserName")
                                                           .build();

         assertTrue(queryUrl.buildRssSearchUrl()
                 .equals("http://loft.spartez.com/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?"
                 + "&priority=1&sorter/field=priority&sorter/order=DESC&pager/start=1&tempMax=100&os_authType=basic"));

     }


    public void testSavedFilterUrl() {
        JIRAQueryFragment query = new JIRASavedFilterBean("Saved Filter Name", 1000);
           JiraQueryUrl queryUrl = new JiraQueryUrl.Builder()
                .serverUrl(SERVER_URL)
                .queryFragment(query)
                .userName("userName")
                .start(0)
                .max(100)
                .sortBy("priority")
                .sortOrder("DESC")
                .build();


        assertTrue(queryUrl.buildSavedFilterUrl().equals("http://loft.spartez.com/sr/jira.issueviews:"
                + "searchrequest-xml/1000/SearchRequest-1000.xml?sorter/field=priority"
                + "&sorter/order=DESC&pager/start=0&tempMax=100&os_authType=basic"));
    }

}