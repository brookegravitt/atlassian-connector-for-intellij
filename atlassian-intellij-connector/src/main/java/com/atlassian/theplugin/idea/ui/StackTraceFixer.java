package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.idea.jira.StackTraceDetector;

/**
 * User: kalamon
 * Date: 10.12.12
 * Time: 10:42
 */
public class StackTraceFixer {
    public static String fixStackTrace(String text) {
        if (!StackTraceDetector.containsStackTrace(text)) {
            return text;
        }
        String fixedStack = text.replaceAll("(\\(.+?\\.java:\\d+\\))\\s*at", "$1\nat");
        return fixedStack.replaceAll("[ \t]+at", "\nat");
    }
}
