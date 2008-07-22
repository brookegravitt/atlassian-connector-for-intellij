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

package com.atlassian.theplugin.idea.jira.editor;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public final class StackTraceDetector {

	private static final String STACK_LINE_PATTERN = "\\(.+\\.java:\\d+\\)";

	private StackTraceDetector() {
	}

	public static boolean containsStackTrace(String txt) {
		Pattern p = Pattern.compile(STACK_LINE_PATTERN);
		Matcher m = p.matcher(txt);
		if (m.find()) {
			return true;
		}
		return false;
	}
}
