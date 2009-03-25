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
package com.atlassian.theplugin.jira.model;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.api.JIRAIssueBean;
import com.intellij.openapi.project.Project;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * User: pmaruszak
 */
public class ActiveJiraIssueImpl implements ActiveJiraIssue {
	private DateTime lastStartTime;
	private Period secondsSpent;
	private final Project project;
	private final JiraServerCfg server;
	private final JIRAIssueBean issue;
	JIRAServerFacade facade = JIRAServerFacadeImpl.getInstance();
	boolean active = false;

	public ActiveJiraIssueImpl(final Project project, final JiraServerCfg server, final JIRAIssueBean issue,
			DateTime lastStartTime, long secondsSpent) {
		this.project = project;
		this.server = server;
		this.issue = issue;
		this.lastStartTime = lastStartTime;
		this.secondsSpent = new Period(secondsSpent, PeriodType.seconds());
	}

	public ActiveJiraIssueImpl(final Project project, final JiraServerCfg server, final JIRAIssueBean issue,
			DateTime lastStartTime) {
		this(project, server, issue, lastStartTime, 0);

	}

	public ActiveJiraIssueImpl(final Project project, final JiraServerCfg server, final JIRAIssueBean issue) {
		this(project, server, issue, new DateTime(), 0);
	}

	public void activate() {
		if (!active) {
			//assign to me and start working
			//IdeaHelper.getIssuesToolWindowPanel(project).startWorkingOnIssue(issue);
			active = true;
			showInTaskbar();
		}
	}

	public void deactivate() {
		active = false;
		hideInTaskbar();
		DateTime now = new DateTime();
		Period nextPeriod = new Period(lastStartTime, now, PeriodType.seconds());
		secondsSpent = secondsSpent.withSeconds(nextPeriod.getSeconds());
		//facade.logWork(server, issue, StringUtil.generateJiraLogTimeString(secondsSpent), );
	}

	public JIRAIssue getIssue() {
		return issue;
	}

	public Period getTimeSpent() {
		return secondsSpent;
	}

	public JiraServerCfg getServer() {
		return server;
	}

	private void showInTaskbar() {

	}

	private void hideInTaskbar() {

	}

}
