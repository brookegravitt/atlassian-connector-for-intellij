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

package com.atlassian.theplugin.idea.jira;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Html2text {

	private static final String HTML_TAG = "<.*?>";
	private static final String LT_TAG = "&lt;";
	private static final String GT_TAG = "&gt;";
	private static final String P_TAG = "<p>";
	private static final String BR_TAG = "<br/?>";

	private Html2text() {
	}

	/**
	 * @param html html string to translate (remove tags)
	 * @return Empty string in case of null input. Otherwise translated value.
	 */
	public static String translate(String html) {
		if (html == null || html.length() == 0) {
			return "";
		}

		Pattern p = Pattern.compile(BR_TAG);
		Matcher m = p.matcher(html);	// matcher does not accept nulls
		String result = m.replaceAll("");

		p = Pattern.compile(P_TAG);
		m = p.matcher(result);
		result = m.replaceAll("\n");

		p = Pattern.compile(HTML_TAG);
		m = p.matcher(result);
		result = m.replaceAll("");

		p = Pattern.compile(LT_TAG);
		m = p.matcher(result);
		result = m.replaceAll("<");

		p = Pattern.compile(GT_TAG);
		m = p.matcher(result);
		result = m.replaceAll(">");

		return result;
	}
}
