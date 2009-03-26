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
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.project.Project;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * User: pmaruszak
 */
public class ActiveJiraIssueImpl implements ActiveJiraIssue {
	private DateTime lastStartTime;
	private Period timeSpent;
	private final Project project;
	private JiraServerCfg server;
	private JIRAIssue issue;
	private boolean active = false;

	public ActiveJiraIssueImpl(final Project project, final JiraServerCfg server, final JIRAIssue issue,
			DateTime lastStartTime, long timeSpent) {
		this.project = project;
		this.server = server;
		this.issue = issue;
		this.lastStartTime = lastStartTime;
		this.timeSpent = new Period(timeSpent, PeriodType.seconds());
	}

	public ActiveJiraIssueImpl(final Project project, final JiraServerCfg server, final JIRAIssue issue,
			DateTime lastStartTime) {
		this(project, server, issue, lastStartTime, 0);

	}

	public ActiveJiraIssueImpl(final Project project, final JiraServerCfg server, final JIRAIssue issue) {
		this(project, server, issue, new DateTime(), 0);
	}

	public void activate() {
		final JiraWorkspaceConfiguration conf = IdeaHelper.getProjectComponent(project, JiraWorkspaceConfiguration.class);
		if (conf != null) {
			conf.setActiveJiraIssue(this);
		}
		if (!active) {
			//assign to me and start working
			//IdeaHelper.getIssuesToolWindowPanel(project).startWorkingOnIssue(issue);
			active = true;
		}
	}

	public void deactivate() {
		active = false;
		recalculateTimeSpent();

	}

	public void resetTimeSpent() {
		timeSpent = new Period(0);
	}


	private void recalculateTimeSpent() {
		DateTime now = new DateTime();
		Period nextPeriod = new Period(lastStartTime, now, PeriodType.seconds());
		timeSpent = timeSpent.withSeconds(nextPeriod.getSeconds() + timeSpent.getSeconds());
		lastStartTime = now;
		//facade.logWork(server, issue, StringUtil.generateJiraLogTimeString(timeSpent), );

	}


	public JIRAIssue getIssue() {
		return issue;
	}

	public Period getTimeSpent() {
		recalculateTimeSpent();
		return timeSpent;
	}

	public JiraServerCfg getServer() {
		return server;
	}

	public void setTimeSpent(final Period timeSpent) {
		this.timeSpent = timeSpent;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public void setIssue(final JIRAIssue issue) {
		this.issue = issue;
	}

	public void setServer(final JiraServerCfg server) {
		this.server = server;
	}
}
