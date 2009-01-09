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
package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.idea.bamboo.build.BuildLogPanel;
import junit.framework.TestCase;

import java.util.regex.Matcher;

/**
 * JavaFileFilter Tester.
 *
 * @author wseliga
 */
public class JavaFileFilterTest extends TestCase {

    public void testApplyFilterNoRowColumn() {
		Matcher match = BuildLogPanel.JavaFileFilter.findMatchings(
				"/fdasf/abc/ddd /lab/pazu/idea/src/test/java/com" +
				"/atlassian/theplugin/idea/bamboo/JavaFileFilterTest.java blablabla");
		assertTrue(match.find());
		assertEquals(
				"/fdasf/abc/ddd /lab/pazu/idea/src/test/java/com/atlassian/theplugin/idea/bamboo/JavaFileFilterTest.java",
				match.group(BuildLogPanel.JavaFileFilter.FULLPATH_GROUP));
		assertNull(match.group(BuildLogPanel.JavaFileFilter.COLUMN_GROUP));
		assertNull(match.group(BuildLogPanel.JavaFileFilter.ROW_GROUP));
	}

	public void testApplyFilterRowOnly() {
		Matcher match = BuildLogPanel.JavaFileFilter.findMatchings(
				"/fdasf/abc/ddd /lab/pazu/idea/src/test/java/com"
				+ "/atlassian/theplugin/idea/bamboo/JavaFileFilterTest.java:456x blablabla");
		assertTrue(match.find());
		assertEquals(
				"/fdasf/abc/ddd /lab/pazu/idea/src/test/java/com/atlassian/theplugin/idea/bamboo/JavaFileFilterTest.java",
				match.group(BuildLogPanel.JavaFileFilter.FULLPATH_GROUP));
		assertNull(match.group(BuildLogPanel.JavaFileFilter.COLUMN_GROUP));
		assertEquals("456", match.group(BuildLogPanel.JavaFileFilter.ROW_GROUP));
	}

	public void testApplyFilterRowAndColumn() {
		Matcher match = BuildLogPanel.JavaFileFilter.findMatchings(
				"/fdasf/abc/ddd /lab/pazu/idea/src/test/java/com"
				+ "/atlassian/theplugin/idea/bamboo/JavaFileFilterTest.java:456:12x blablabla");
		assertTrue(match.find());
		assertEquals(
				"/fdasf/abc/ddd /lab/pazu/idea/src/test/java/com/atlassian/theplugin/idea/bamboo/JavaFileFilterTest.java",
				match.group(BuildLogPanel.JavaFileFilter.FULLPATH_GROUP));
		assertEquals("12", match.group(BuildLogPanel.JavaFileFilter.COLUMN_GROUP));
		assertEquals("456", match.group(BuildLogPanel.JavaFileFilter.ROW_GROUP));
	}

	public void testApplyFilterRowAndColumn2() {
		Matcher match = BuildLogPanel.JavaFileFilter.findMatchings(
				"/fdasf/abc/ddd /lab/pazu/idea/src/test/java/com"
				+ "/atlassian/theplugin/idea/bamboo/JavaFileFilterTest.java:[456,13] blablabla");
		assertTrue(match.find());
		assertEquals(
				"/fdasf/abc/ddd /lab/pazu/idea/src/test/java/com/atlassian/theplugin/idea/bamboo/JavaFileFilterTest.java",
				match.group(BuildLogPanel.JavaFileFilter.FULLPATH_GROUP));
		assertEquals("13", match.group(BuildLogPanel.JavaFileFilter.COLUMN_GROUP));
		assertEquals("456", match.group(BuildLogPanel.JavaFileFilter.ROW_GROUP));
	}

	public void testApplyFilterMissingColon() {
		Matcher match = BuildLogPanel.JavaFileFilter.findMatchings(
				"/fdasf/abc/ddd /lab/pazu/idea/src/test/java/com"
				+ "/atlassian/theplugin/idea/bamboo/JavaFileFilterTest.java[456,13] blablabla");
		assertTrue(match.find());
		assertEquals(
				"/fdasf/abc/ddd /lab/pazu/idea/src/test/java/com/atlassian/theplugin/idea/bamboo/JavaFileFilterTest.java",
				match.group(BuildLogPanel.JavaFileFilter.FULLPATH_GROUP));
		assertNull(match.group(BuildLogPanel.JavaFileFilter.COLUMN_GROUP));
		assertNull(match.group(BuildLogPanel.JavaFileFilter.ROW_GROUP));
	}

}
