package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import junit.framework.TestCase;


public class IssueRecentlyOpenBeanTest extends TestCase {
	private IssueRecentlyOpenBean issue11;
	private IssueRecentlyOpenBean issue11copy;
	private IssueRecentlyOpenBean issue12;
	private IssueRecentlyOpenBean issue21;

	public void setUp() throws Exception {
		final ServerIdImpl serverId = new ServerIdImpl();
		issue11 = new IssueRecentlyOpenBean(serverId, "1", "issueUrl");
		issue11copy = new IssueRecentlyOpenBean(serverId, "1", "issueUrl");
		issue12 = new IssueRecentlyOpenBean(serverId, "2", "issueUrl");
		issue21 = new IssueRecentlyOpenBean(new ServerIdImpl(), "1", "issueUrl");
	}

	public void testEquals() throws Exception {
		assertEquals(issue11, issue11);
		assertEquals(issue11, issue11copy);

		assertFalse(issue11.equals(issue12));
		assertFalse(issue11.equals(issue21));
	}


}
