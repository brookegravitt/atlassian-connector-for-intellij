package com.atlassian.theplugin.jira.cache;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import junit.framework.TestCase;


public class RecentlyOpenIssuesCacheTest extends TestCase {
	private RecentlyOpenIssuesCache cache;
	private JiraServerData server;
	private JiraIssueAdapter issue1;
	private JiraIssueAdapter issue2;
	private JiraIssueAdapter issue3;

	public void setUp() throws Exception {
		cache = new RecentlyOpenIssuesCache(null, new JiraWorkspaceConfiguration());
		server = new JiraServerData(new JiraServerCfg(true, "server", "", new ServerIdImpl(), true) {
			public ServerType getServerType() {
				return null;
			}

			public JiraServerCfg getClone() {
				return null;
			}
		});

		issue1 = new JiraIssueAdapter(server);
		issue1.setKey("1");

		issue2 = new JiraIssueAdapter(server);
		issue2.setKey("2");

		issue3 = new JiraIssueAdapter(server);
		issue3.setKey("3");
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testLoadRecenltyOpenIssues() {
		/*final JiraWorkspaceConfiguration conf = new JiraWorkspaceConfiguration();
		conf.addRecentlyOpenIssue(issue1);
		conf.addRecentlyOpenIssue(issue2);
		
		cache = new RecentlyOpenIssuesCache(null, conf);*/

		// can't test it as loadRecenltyOpenIssues uses facade while RecentlyOpenIssuesCache is project component
		// I don't want to inject facade with setter used only by test method
		// The solution would be to transform facade to project or application component
	}

	public void testGetLoadedRecenltyOpenIssues() throws Exception {
		cache.addIssue(issue1);
		cache.addIssue(issue2);

		assertEquals(2, cache.getLoadedRecenltyOpenIssues().size());
		assertTrue(cache.getLoadedRecenltyOpenIssues().contains(issue1));
		assertTrue(cache.getLoadedRecenltyOpenIssues().contains(issue2));
		assertFalse(cache.getLoadedRecenltyOpenIssues().contains(issue3));

		// test order
		assertEquals(issue2, cache.getLoadedRecenltyOpenIssues().get(0));
		assertEquals(issue1, cache.getLoadedRecenltyOpenIssues().get(1));
	}

	public void testGetLoadedRecenltyOpenIssue() throws Exception {
		assertNull(cache.getLoadedRecenltyOpenIssue(issue1.getKey(),
                issue1.getJiraServerData().getServerId(), issue1.getIssueUrl()));

		cache.addIssue(issue1);
		cache.addIssue(issue2);

		assertEquals(issue1, cache.getLoadedRecenltyOpenIssue(issue1.getKey(),
                issue1.getJiraServerData().getServerId(), issue1.getIssueUrl()));
	}

	public void testAddIssue() {
		// add issue
		cache.addIssue(issue1);

		assertEquals(1, cache.getLoadedRecenltyOpenIssues().size());
		assertEquals(issue1, cache.getLoadedRecenltyOpenIssues().get(0));

		// add the same issue again
		cache.addIssue(issue1);

		assertEquals(1, cache.getLoadedRecenltyOpenIssues().size());
		assertEquals(issue1, cache.getLoadedRecenltyOpenIssues().get(0));
	}

	public void testAddIssueOrder() {
		// add issues
		cache.addIssue(issue1);
		cache.addIssue(issue2);

		assertEquals(2, cache.getLoadedRecenltyOpenIssues().size());

		// last added issue should be first on the list
		assertEquals(issue2, cache.getLoadedRecenltyOpenIssues().get(0));
		assertEquals(issue1, cache.getLoadedRecenltyOpenIssues().get(1));

		// add issue1 again (should jump on the firs position)
		cache.addIssue(issue1);

		assertEquals(2, cache.getLoadedRecenltyOpenIssues().size());
		assertEquals(issue1, cache.getLoadedRecenltyOpenIssues().get(0));
		assertEquals(issue2, cache.getLoadedRecenltyOpenIssues().get(1));
	}

	public void testAddIssueLimit() {

		cache.addIssue(issue1);
		cache.addIssue(issue2);
		for (int i = 0; i < JiraWorkspaceConfiguration.RECENLTY_OPEN_ISSUES_LIMIT - 2; ++i) {
			JiraIssueAdapter issue = new JiraIssueAdapter(server);
			issue.setKey(Integer.toString(i + 100));
			cache.addIssue(issue);
		}
		cache.addIssue(issue3);

		assertEquals(JiraWorkspaceConfiguration.RECENLTY_OPEN_ISSUES_LIMIT, cache.getLoadedRecenltyOpenIssues().size());
		// make sure issue3 is on top
		assertEquals(issue3, cache.getLoadedRecenltyOpenIssues().get(0));
		// make sure issue2 is the last one (issue1 has been cut)
		assertEquals(issue2,
				cache.getLoadedRecenltyOpenIssues().get(JiraWorkspaceConfiguration.RECENLTY_OPEN_ISSUES_LIMIT - 1));
	}


	public void testUpdateIssue() {
		assertEquals(0, cache.getLoadedRecenltyOpenIssues().size());
		cache.updateIssue(issue1);
		assertEquals(0, cache.getLoadedRecenltyOpenIssues().size());

		cache.addIssue(issue1);
		assertEquals(1, cache.getLoadedRecenltyOpenIssues().size());
		assertEquals(issue1, cache.getLoadedRecenltyOpenIssues().get(0));

		cache.updateIssue(issue1);
		assertEquals(1, cache.getLoadedRecenltyOpenIssues().size());
		assertEquals(issue1, cache.getLoadedRecenltyOpenIssues().get(0));
	}

	public void testUpdateIssueOrder() {
		cache.addIssue(issue1);
		cache.addIssue(issue2);
		assertEquals(2, cache.getLoadedRecenltyOpenIssues().size());
		assertEquals(issue2, cache.getLoadedRecenltyOpenIssues().get(0));
		assertEquals(issue1, cache.getLoadedRecenltyOpenIssues().get(1));

		// updating issue should not change the order (the opposite of add operation)
		cache.updateIssue(issue1);
		assertEquals(issue2, cache.getLoadedRecenltyOpenIssues().get(0));
		assertEquals(issue1, cache.getLoadedRecenltyOpenIssues().get(1));
	}

}
