package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.commons.jira.api.soap.axis.RemoteIssue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:35:22 PM
 */
public class DueDateFiller implements FieldFiller {
	public List<String> getFieldValues(String field, JIRAIssue detailedIssue) {
		RemoteIssue ri = (RemoteIssue) detailedIssue.getRawSoapIssue();
		if (ri == null) {
			return null;
		}
		Calendar dueDate = ri.getDuedate();
		if (dueDate == null) {
			return null;
		}
		// hmm, is it right for all cases? This is the date format JIRA is sending us, will it accept it back?
		DateFormat df = new SimpleDateFormat("dd/MMM/yy", Locale.US);
		List<String> result = new ArrayList<String>();
		result.add(df.format(dueDate.getTime()));
		return result;
	}
}
