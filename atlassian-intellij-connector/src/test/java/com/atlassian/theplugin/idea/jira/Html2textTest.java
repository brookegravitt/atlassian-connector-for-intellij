package com.atlassian.theplugin.idea.jira;

import junit.framework.TestCase;
import com.atlassian.theplugin.idea.util.Html2text;


public class Html2textTest extends TestCase {

	public void setUp() throws Exception {
		super.setUp();
	}

	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testTranslate() {
		assertEquals("", Html2text.translate(null));
		assertEquals("", Html2text.translate(""));
		assertEquals(" ", Html2text.translate(" "));

		assertEquals("cztery litery maryni", Html2text.translate("cztery litery maryni"));

		assertEquals("", Html2text.translate("<html>"));
		assertEquals("", Html2text.translate("<br>"));
		assertEquals("\n", Html2text.translate("<p>"));
		assertEquals("<", Html2text.translate("&lt;"));
		assertEquals(">", Html2text.translate("&gt;"));

		assertEquals("", Html2text.translate("<any tag>"));
	}


}
