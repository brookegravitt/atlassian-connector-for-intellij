package com.atlassian.theplugin.configuration;

import com.atlassian.theplugin.commons.cfg.ServerId;
import junit.framework.TestCase;


public class IssueRecentlyOpenBeanTest extends TestCase {
	private IssueRecentlyOpenBean issue11;
	private IssueRecentlyOpenBean issue11copy;
	private IssueRecentlyOpenBean issue12;
	private IssueRecentlyOpenBean issue21;

	public void setUp() throws Exception {
		final String serverId = new ServerId().getStringId();
		issue11 = new IssueRecentlyOpenBean(serverId, "1");
		issue11copy = new IssueRecentlyOpenBean(serverId, "1");
		issue12 = new IssueRecentlyOpenBean(serverId, "2");
		issue21 = new IssueRecentlyOpenBean(new ServerId().getStringId(), "1");
	}

	public void testEquals() throws Exception {
		assertEquals(issue11, issue11);
		assertEquals(issue11, issue11copy);

		assertFalse(issue11.equals(issue12));
		assertFalse(issue11.equals(issue21));
	}


}
