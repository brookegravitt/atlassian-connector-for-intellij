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

package com.atlassian.theplugin.idea.jira.table.renderers;

import com.atlassian.theplugin.idea.jira.JiraIssueAdapter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class ExtendedIssueInfoCellRenderer extends DefaultTableCellRenderer {
	public static final String BODY_WITH_STYLE =
			"<body style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">";
	private static final int MAX_LINE_LENGTH = 50;

	private String buildTolltip(JiraIssueAdapter jiraIssue) {
		StringBuilder sb = new StringBuilder(
                "<html>"
                + BODY_WITH_STYLE);

		sb.append("<table width=\"100%\">");
		sb.append("<tr><td colspan=5><b><font color=blue>");
        sb.append(jiraIssue.getKey());
        sb.append("</font></b>");

		sb.append("<tr><td valign=\"top\"><b>Summary:</b></td><td valign=\"top\">");
		String summary = jiraIssue.getSummary();
		if (summary.length() > MAX_LINE_LENGTH) {
			summary = summary.substring(0, MAX_LINE_LENGTH) + "...";
		}
		sb.append(summary);
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Description:</b></td><td valign=\"top\">");
		String description = jiraIssue.getDescription();
		if (description.length() > MAX_LINE_LENGTH) {
			description = description.substring(0, MAX_LINE_LENGTH) + "...";
		}
		sb.append(description);
		sb.append("");
		sb.append("</td></tr>");

        sb.append("<tr><td valign=\"top\"><b>Status:</b></td><td valign=\"top\">");
        sb.append(jiraIssue.getStatus());
        sb.append("");
        sb.append("</td></tr>");

        sb.append("<tr><td valign=\"top\"><b>Reporter:</b></td><td valign=\"top\">");
		sb.append(jiraIssue.getReporter());
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Assignee:</b></td><td valign=\"top\">");
		sb.append(jiraIssue.getAssignee());
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Resolution:</b></td><td valign=\"top\">");
		sb.append(jiraIssue.getResolution());
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Created:</b></td><td valign=\"top\">");
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		DateFormat ds = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		try {
			sb.append(ds.format(df.parse(jiraIssue.getCreated())));
		} catch (ParseException e) {
			sb.append("Invalid");
		}
		sb.append("");
		sb.append("</td></tr>");

		sb.append("<tr><td valign=\"top\"><b>Updated:</b></td><td valign=\"top\">");
		try {
			sb.append(ds.format(df.parse(jiraIssue.getUpdated())));
		} catch (ParseException e) {
			sb.append("Invalid");
		}
		sb.append("");
		sb.append("</td></tr>");

		sb.append("</table>");
		sb.append("</body></html>");
		return sb.toString();
	}


	public Component getTableCellRendererComponent(JTable jTable,
												   Object o, boolean isSelected, boolean hasFocus, int i, int i1) {
		Component c = super.getTableCellRendererComponent(jTable, o, isSelected, hasFocus, i, i1);
		if (o instanceof JiraIssueAdapter) {
			JiraIssueAdapter issueAdapter = (JiraIssueAdapter) o;
			((JLabel) c).setToolTipText(buildTolltip(issueAdapter));
			((JLabel) c).setText(issueAdapter.getSummary());
			((JLabel) c).setIcon(null);
		}
		return c;
	}
}