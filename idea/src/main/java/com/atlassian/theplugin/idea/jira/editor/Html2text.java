package com.atlassian.theplugin.idea.jira.editor;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public final class Html2text {

	private static final String HTML_TAG = "<.*>";
	private static final String LT_TAG = "&lt;";
	private static final String GT_TAG = "&gt;";
	private static final String P_TAG = "<p>";
	private static final String BR_TAG = "<br/?>";

	private Html2text() {
	}

	public static String translate(String html) {
		Pattern p = Pattern.compile(BR_TAG);
		Matcher m = p.matcher(html);
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
