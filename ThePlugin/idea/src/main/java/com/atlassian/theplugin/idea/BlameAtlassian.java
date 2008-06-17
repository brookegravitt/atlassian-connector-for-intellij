package com.atlassian.theplugin.idea;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;

import java.awt.Component;


public class BlameAtlassian extends ErrorReportSubmitter {
	public String getReportActionText() {
		return "Create error report in Atlassian JIRA";
	}

	public SubmittedReportInfo submit(IdeaLoggingEvent[] ideaLoggingEvents, Component component) {
		StringBuilder description = new StringBuilder();
		for (IdeaLoggingEvent ideaLoggingEvent : ideaLoggingEvents) {
			description.append(ideaLoggingEvent.getMessage());
			description.append("\n");
			description.append(ideaLoggingEvent.getThrowableText());
			description.append("\n");
		}
		BrowserUtil.launchBrowser(BugReporting.getBugWithDescriptionUrl(description.toString()));
		return new SubmittedReportInfo(null, "JIRA ticket", SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);
	}
}
