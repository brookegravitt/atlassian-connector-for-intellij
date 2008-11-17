package com.atlassian.theplugin.idea.fisheye;

import junit.framework.TestCase;

public class SourceCodeLinkParserTest extends TestCase {
	public void testLinkParser() {
		SourceCodeLinkParser p = new SourceCodeLinkParser("?");
		p.parse();
		assertNull(p.getPath());
		assertNull(p.getRevision());
		assertEquals(0, p.getLine());

		p = new SourceCodeLinkParser("");
		p.parse();
		assertNull(p.getPath());
		assertNull(p.getRevision());
		assertEquals(0, p.getLine());

		p = new SourceCodeLinkParser("?r=#");
		p.parse();
		assertNull(p.getPath());
		assertNull(p.getRevision());
		assertEquals(0, p.getLine());

		p = new SourceCodeLinkParser("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java?r=20900#l90");
		p.parse();
		assertEquals("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java", p.getPath());
		assertEquals("20900", p.getRevision());
		assertEquals(89, p.getLine());

		p = new SourceCodeLinkParser("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java?r=20900#l90x");
		p.parse();
		assertEquals("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java", p.getPath());
		assertEquals("20900", p.getRevision());
		assertEquals(0, p.getLine());		

		p = new SourceCodeLinkParser("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java?r=20900");
		p.parse();
		assertEquals("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java", p.getPath());
		assertEquals("20900", p.getRevision());
		assertEquals(0, p.getLine());

		p = new SourceCodeLinkParser("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java?r=20900#l");
		p.parse();
		assertEquals("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java", p.getPath());
		assertEquals("20900", p.getRevision());
		assertEquals(0, p.getLine());

		p = new SourceCodeLinkParser("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java?r=20900#");
		p.parse();
		assertEquals("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java", p.getPath());
		assertEquals("20900", p.getRevision());
		assertEquals(0, p.getLine());

		p = new SourceCodeLinkParser("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java?");
		p.parse();
		assertEquals("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java", p.getPath());
		assertNull(p.getRevision());
		assertEquals(0, p.getLine());

		p = new SourceCodeLinkParser("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java");
		p.parse();
		assertEquals("https://studio.atlassian.com/source/browse/PL/api/JIRAIssueBean.java", p.getPath());
		assertNull(p.getRevision());
		assertEquals(0, p.getLine());
	}
}
