package com.atlassian.theplugin.util;

import junit.framework.TestCase;

/**
 * User: kalamon
 * Date: Apr 24, 2009
 * Time: 3:24:17 PM
 */
public class HtmlizerTest extends TestCase {
    public void testHtmlizeNoHyperlinks() {
        String txt = "some text";
        String result = new Htmlizer().htmlizeHyperlinks(txt);
        assertEquals(txt, result);
    }

    public void testHtmlizeSimpleHyperlink() {
        String txt = "http://test.com";
        String expected = "<a href=\"http://test.com\">http://test.com</a>";
        String result = new Htmlizer().htmlizeHyperlinks(txt);
        assertEquals(expected, result);
    }

    public void testHtmlizeTwoHyperlinks() {
        String txt = "http://a.com https://b.net";
        String expected = "<a href=\"http://a.com\">http://a.com</a> <a href=\"https://b.net\">https://b.net</a>";
        String result = new Htmlizer().htmlizeHyperlinks(txt);
        assertEquals(expected, result);
    }

    public void testHtmlizeTwoHyperlinksAndSomeText() {
        String txt = " aaa http://a.com bbb ccc https://b.net ddd";
        String expected = " aaa <a href=\"http://a.com\">http://a.com</a> bbb ccc <a href=\"https://b.net\">https://b.net</a> ddd";
        String result = new Htmlizer().htmlizeHyperlinks(txt);
        assertEquals(expected, result);
    }

    public void testHtmlizeTextBeforeHyperlink() {
        String txt = "test http://test.com";
        String expected = "test <a href=\"http://test.com\">http://test.com</a>";
        String result = new Htmlizer().htmlizeHyperlinks(txt);
        assertEquals(expected, result);
    }

    public void testHtmlizeTextAfterHyperlink() {
        String txt = "http://test.com test";
        String expected = "<a href=\"http://test.com\">http://test.com</a> test";
        String result = new Htmlizer().htmlizeHyperlinks(txt);
        assertEquals(expected, result);
    }

    public void testHtmlizeTextBeforeAndAfterHyperlink() {
        String txt = "aaa http://test.com bbb";
        String expected = "aaa <a href=\"http://test.com\">http://test.com</a> bbb";
        String result = new Htmlizer().htmlizeHyperlinks(txt);
        assertEquals(expected, result);
    }

    public void testHtmlizeAlreadyHyperlinkedText() {
        String expected = "<a href=\"http://test.com\">ccc</a>";
        String result = new Htmlizer().htmlizeHyperlinks(expected);
        assertEquals(expected, result);
    }

    public void testHtmlizeHyperlinkAndAlreadyHyperlinkedText() {
        String txt = "http://a.com <a href=\"http://b.com\">bbb</a>";
        String expected = "<a href=\"http://a.com\">http://a.com</a> <a href=\"http://b.com\">bbb</a>";
        String result = new Htmlizer().htmlizeHyperlinks(txt);
        assertEquals(expected, result);
    }

    public void testHtmlizeAlreadyHyperlinkedTextAndHyperlink() {
        String txt = "<a href=\"http://b.com\">bbb</a> http://a.com";
        String expected = "<a href=\"http://b.com\">bbb</a> <a href=\"http://a.com\">http://a.com</a>";
        String result = new Htmlizer().htmlizeHyperlinks(txt);
        assertEquals(expected, result);
    }

    public void testHtmlizeComplicatedText() {
        String txt = " abc\naaa <a href=\"http://b.com\">bbb</a>\t\nhttp://a.com <a href=\"http://c.com\">ccc</a> https://a.net";
        String expected = " abc\n" +
                "aaa <a href=\"http://b.com\">bbb</a>\t\n" +
                "<a href=\"http://a.com\">http://a.com</a> <a href=\"http://c.com\">ccc</a> <a href=\"https://a.net\">https://a.net</a>";
        String result = new Htmlizer().htmlizeHyperlinks(txt);
        assertEquals(expected, result);
    }
}
