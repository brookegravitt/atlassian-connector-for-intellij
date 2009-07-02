package com.atlassian.theplugin.idea.jira;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * User: kalamon
 * Date: May 12, 2009
 * Time: 1:26:22 PM
 */
public final class JiraTimeFormatter {

    private JiraTimeFormatter() { }

    public static String formatTimeFromJiraTimeString(String dateString) {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z (z)", Locale.US);
        DateFormat ds = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String t;
        try {
            t = ds.format(df.parse(dateString));
        } catch (ParseException e) {
            t = "Invalid";
        }

        return t;
    }
}
