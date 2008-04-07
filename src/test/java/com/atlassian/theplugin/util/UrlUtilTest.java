package com.atlassian.theplugin.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.net.MalformedURLException;


public class UrlUtilTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

	public void testAddHttpPrefix() {

		String url = null;
		assertEquals(url, UrlUtil.addHttpPrefix(url));

		url = "";
		assertEquals(url, UrlUtil.addHttpPrefix(url));

		url = "some url";
		assertEquals("http://" + url, UrlUtil.addHttpPrefix(url));

		url = "http://some url";
		assertEquals(url, UrlUtil.addHttpPrefix(url));

		url = "https://some url";
		assertEquals(url, UrlUtil.addHttpPrefix(url));
	}

	public void testRemoveUrlTrailingSlashes() {

		String url = null;
		assertEquals(url, UrlUtil.removeUrlTrailingSlashes(url));

		url = "";
		assertEquals(url, UrlUtil.removeUrlTrailingSlashes(url));

		url = "/";
		assertEquals(url, UrlUtil.removeUrlTrailingSlashes(url));

		url = "////";
		assertEquals(url, UrlUtil.removeUrlTrailingSlashes(url));

		url = "some url";
		assertEquals(url, UrlUtil.removeUrlTrailingSlashes(url));

		url = "some url/";
		assertEquals(url, UrlUtil.removeUrlTrailingSlashes(url));

		url = "some url////";
		assertEquals(url, UrlUtil.removeUrlTrailingSlashes(url));

		url = "http://xlaski.pl";
		assertEquals(url, UrlUtil.removeUrlTrailingSlashes(url));

		url = "http://xlaski.pl";
		assertEquals(url, UrlUtil.removeUrlTrailingSlashes(url + "/"));
		assertEquals(url, UrlUtil.removeUrlTrailingSlashes(url + "///////"));

	}

	public void testValidateUrl() {

		String url = null;
		try {
			UrlUtil.validateUrl(url);
			fail("null url validation failed");
		} catch (MalformedURLException e) {
			// ok
		}

		url = "";
		try {
			UrlUtil.validateUrl(url);
			fail("empty url validation failed");
		} catch (MalformedURLException e) {
			// ok
		}

		url = "dupa maryni";
		try {
			UrlUtil.validateUrl(url);
			fail("malformed url validation failed");
		} catch (MalformedURLException e) {
			// ok
		}

		url = "xlaski.pl";
		try {
			UrlUtil.validateUrl(url);
			fail("malformed url validation failed");
		} catch (MalformedURLException e) {
			// ok
		}

		url = "www.xlaski.pl";
		try {
			UrlUtil.validateUrl(url);
			fail("malformed url validation failed");
		} catch (MalformedURLException e) {
			// ok
		}

		url = "http://xlaski.pl";
		try {
			UrlUtil.validateUrl(url);
			// ok
		} catch (MalformedURLException e) {
			fail("correct url validation failed");
		}

		url = "http://www.xlaski.pl";
		try {
			UrlUtil.validateUrl(url);
			// ok
		} catch (MalformedURLException e) {
			fail("correct url validation failed");
		}

		url = "http://www.xlaski.pl:8080";
		try {
			UrlUtil.validateUrl(url);
			// ok
		} catch (MalformedURLException e) {
			fail("correct url validation failed");
		}

		url = "https://www.xlaski.pl:8080";
		try {
			UrlUtil.validateUrl(url);
			// ok
		} catch (MalformedURLException e) {
			fail("correct url validation failed");
		}

	}

}
