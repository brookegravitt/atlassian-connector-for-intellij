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

public class UtilTest extends TestCase {
	public void testTextToMultilineHtml() {
		assertNull(Util.textToMultilineHtml(null));
		assertEquals("abc", Util.textToMultilineHtml("abc"));
		assertEquals("abc def", Util.textToMultilineHtml("abc def"));
		assertEquals("abc&gt;&lt;def", Util.textToMultilineHtml("abc><def"));
		assertEquals("abc<br />def", Util.textToMultilineHtml("abc\ndef"));
		assertEquals("abc&nbsp; &nbsp; def", Util.textToMultilineHtml("abc    def"));
		assertEquals("abc<br /><br />&nbsp;&nbsp;&nbsp;&nbsp; def&nbsp; x", Util.textToMultilineHtml("abc\n\n\tdef  x"));
	}
}
