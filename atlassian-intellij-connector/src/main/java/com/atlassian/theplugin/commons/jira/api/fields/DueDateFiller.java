package com.atlassian.theplugin.commons.jira.api.fields;

import com.atlassian.connector.commons.jira.JIRAIssue;
import com.atlassian.connector.commons.jira.soap.axis.RemoteIssue;
import com.atlassian.jira.rest.client.domain.Issue;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * User: jgorycki
 * Date: Apr 6, 2009
 * Time: 4:35:22 PM
 */
public class DueDateFiller extends AbstractFieldFiller {
    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, Issue apiIssueObject) {
        DateTime dueDate = apiIssueObject.getDueDate();
        if (dueDate == null) {
            return null;
        }
        // hmm, is it right for all cases? This is the date format JIRA is sending us, will it accept it back?
        DateFormat df = new SimpleDateFormat("dd/MMM/yy", Locale.US);
        return ImmutableList.of(df.format(dueDate.toDate()));
    }

    @Override
    protected List<String> getFieldValues(String field, JIRAIssue detailedIssue, RemoteIssue apiIssueObject) {
        Calendar dueDate = apiIssueObject.getDuedate();
        if (dueDate == null) {
            return null;
        }
        // hmm, is it right for all cases? This is the date format JIRA is sending us, will it accept it back?
        DateFormat df = new SimpleDateFormat("dd/MMM/yy", Locale.US);
        return ImmutableList.of(df.format(dueDate.getTime()));
    }
}
