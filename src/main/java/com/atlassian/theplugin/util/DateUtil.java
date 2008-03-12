package com.atlassian.theplugin.util;

import com.atlassian.theplugin.idea.TableColumnInfo;
import org.joda.time.Period;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Mar 11, 2008
 * Time: 4:55:29 PM
 * Taken from the Bamboo project.
 */
public abstract class DateUtil extends TableColumnInfo {
	private static final String PRIOR_TEXT = "before";
	//private static final Category LOGGER = Logger.getInstance(PluginStatusBarToolTip.class);
	public static final int SECONDS_IN_MINUTE = 60;
	public static final int MILISECONDS_IN_SECOND = 1000;

	public static String getRelativePastDate(Date comparedTo, Date someDate) {
		if (someDate != null) {
			Period period = new Period(someDate.getTime(), comparedTo.getTime());
			StringBuffer buffer = new StringBuffer();

			int years = period.getYears();
			if (years > 0) {
				return formatRelativeDateItem(buffer, years, " year");
			}

			int months = period.getMonths();
			if (months > 0) {
				return formatRelativeDateItem(buffer, months, " month");
			}

			int weeks = period.getWeeks();
			if (weeks > 0) {
				return formatRelativeDateItem(buffer, weeks, " week");
			}

			int days = period.getDays();
			if (days > 0) {
				return formatRelativeDateItem(buffer, days, " day");
			}

			int hours = period.getHours();
			if (hours > 0) {
				return formatRelativeDateItem(buffer, hours, " hour");
			}
			int minutes = period.getMinutes();
			if (minutes > 0) {
				return formatRelativeDateItem(buffer, minutes, " minute");
			}

			int seconds = period.getSeconds();
			if (seconds > 0) {
				return formatRelativeDateItem(buffer, seconds, " second");
			}

			if (someDate.getTime() > comparedTo.getTime()) {
				return "in the future";
			}

			return "< 1 second " + PRIOR_TEXT;
		} else {
			PluginUtil.getLogger().warn("Returning a blank string for relative date.");
			return "";
		}

	}

	private static String formatRelativeDateItem(StringBuffer buffer, int numberOfItems, String item) {
		buffer.append(numberOfItems).append(item);
		if (numberOfItems > 1) {
			buffer.append("s");
		}
		buffer.append(" " + PRIOR_TEXT);
		return buffer.toString();
	}
}
