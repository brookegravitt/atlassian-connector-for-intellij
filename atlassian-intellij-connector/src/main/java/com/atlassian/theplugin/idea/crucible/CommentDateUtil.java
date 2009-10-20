package com.atlassian.theplugin.idea.crucible;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * User: jgorycki
 * Date: Mar 6, 2009
 * Time: 3:39:55 PM
 */
public final class CommentDateUtil {

	private CommentDateUtil() { }

	public static String getDateText(Date date) {
		DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.US);
		DateFormat dfo = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		String t;
		try {
			t = dfo.format(df.parse(date.toString()));
		} catch (java.text.ParseException e) {
			t = "Invalid date: " + date.toString();
		}
		return t;
	}
}
