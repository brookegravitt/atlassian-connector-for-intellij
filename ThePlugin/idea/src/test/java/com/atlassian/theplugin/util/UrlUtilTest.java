/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.util;

import junit.framework.TestCase;

import java.net.MalformedURLException;

import com.atlassian.theplugin.commons.util.UrlUtil;


public class UrlUtilTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

	public void testFailMe() {
		assertTrue(false);
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
