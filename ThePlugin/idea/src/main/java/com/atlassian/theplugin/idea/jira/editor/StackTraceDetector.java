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
