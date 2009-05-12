package com.atlassian.theplugin.configuration;

import junit.framework.TestCase;


public class IssueRecentlyOpenBeanTest extends TestCase {
	private IssueRecentlyOpenBean issue11;
	private IssueRecentlyOpenBean issue11copy;
	private IssueRecentlyOpenBean issue12;
	private IssueRecentlyOpenBean issue21;

	public void setUp() throws Exception {
		issue11 = new IssueRecentlyOpenBean("1", "1");
		issue11copy = new IssueRecentlyOpenBean("1", "1");
		issue12 = new IssueRecentlyOpenBean("1", "2");
		issue21 = new IssueRecentlyOpenBean("2", "1");
	}

	public void testEquals() throws Exception {
		assertEquals(issue11, issue11);
		assertEquals(issue11, issue11copy);

		assertFalse(issue11.equals(issue12));
		assertFalse(issue11.equals(issue21));
	}


}
