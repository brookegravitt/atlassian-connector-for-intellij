package com.atlassian.theplugin.idea;

import junit.framework.TestCase;

public class HelpUrlTest extends TestCase {
	public void testGetGoodHelpTopic() {
		String s = HelpUrl.getHelpUrl(Constants.HELP_CONFIG_PANEL);
		assertNotNull(s);
		assertTrue(s.startsWith("http://confluence.atlassian.com"));
		assertTrue(s.contains("IDEPLUGIN"));
	}

	public void testGetBadHelpTopic() {
		assertNull(HelpUrl.getHelpUrl("dupa"));
	}
}
