package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.jira.api.JIRAIssueBean;
import junit.framework.TestCase;


public class RecenltyOpenIssuesTest extends TestCase {
	private JiraWorkspaceConfiguration conf;
	private ServerData server;
	private JIRAIssueBean issue1;
	private JIRAIssueBean issue2;
	private JIRAIssueBean issue3;

	public void setUp() throws Exception {
		conf = new JiraWorkspaceConfiguration();
		server = new ServerData("server", new ServerIdImpl(), "", "", "");

		issue1 = new JIRAIssueBean(server);
		issue1.setKey("1");

		issue2 = new JIRAIssueBean(server);
		issue2.setKey("2");

		issue3 = new JIRAIssueBean(server);
		issue3.setKey("3");
	}

	public void testEmptyConfiguration() {
		assertNotNull(conf.getRecentlyOpenIssuess());
		assertEquals(0, conf.getRecentlyOpenIssuess().size());
	}

	public void testAddRecentlyOpenIssue() {
		conf.addRecentlyOpenIssue(issue1);
		assertEquals(1, conf.getRecentlyOpenIssuess().size());
		assertTrue(conf.getRecentlyOpenIssuess().contains(getRecenltyOpenIssueBean(issue1)));
	}

	public void testAddRecentlyOpenIssueOrder() {
		conf.addRecentlyOpenIssue(issue1);
		conf.addRecentlyOpenIssue(issue2);

		assertEquals(2, conf.getRecentlyOpenIssuess().size());
		assertTrue(conf.getRecentlyOpenIssuess().contains(getRecenltyOpenIssueBean(issue1)));
		assertTrue(conf.getRecentlyOpenIssuess().contains(getRecenltyOpenIssueBean(issue2)));

		// test order
		assertEquals(getRecenltyOpenIssueBean(issue2), conf.getRecentlyOpenIssuess().get(0));
		assertEquals(getRecenltyOpenIssueBean(issue1), conf.getRecentlyOpenIssuess().get(1));
	}

	public void testAddRecenltyOpenIssueLimit() {
		conf.addRecentlyOpenIssue(issue1);
		conf.addRecentlyOpenIssue(issue2);

		for (int i = 0; i < JiraWorkspaceConfiguration.RECENLTY_OPEN_ISSUES_LIMIT - 2; ++i) {
			final JIRAIssueBean issue = new JIRAIssueBean(server);
			issue.setKey("100" + i);
			conf.addRecentlyOpenIssue(issue);
		}

		conf.addRecentlyOpenIssue(issue3);

		// check limit
		assertEquals(JiraWorkspaceConfiguration.RECENLTY_OPEN_ISSUES_LIMIT, conf.getRecentlyOpenIssuess().size());
		// last added issue should be on top
		assertEquals(getRecenltyOpenIssueBean(issue3), conf.getRecentlyOpenIssuess().getFirst());
		// first added issue should disappear (out of the limit)
		assertFalse(conf.getRecentlyOpenIssuess().contains(getRecenltyOpenIssueBean(issue1)));
		// issue2 should be at the end
		assertEquals(getRecenltyOpenIssueBean(issue2), conf.getRecentlyOpenIssuess().getLast());
	}

	private IssueRecentlyOpenBean getRecenltyOpenIssueBean(final JIRAIssueBean issue) {
		return new IssueRecentlyOpenBean(issue.getServer().getServerId(), issue.getKey());
	}
}
