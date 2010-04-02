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

import com.atlassian.theplugin.commons.cfg.ServerId;
import com.atlassian.theplugin.configuration.IssueRecentlyOpenBean;
import com.intellij.util.xmlb.annotations.Transient;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * User: pmaruszak
 */
public class ActiveJiraIssueBean extends IssueRecentlyOpenBean implements ActiveJiraIssue {
    public enum ActivationSource {
        CONNECTOR,
        INTELLIJ
    }
    private ActivationSource source = ActivationSource.CONNECTOR;
	private DateTime lastStartTime = new DateTime();
	private long secondsSpent;
	private boolean active = false;
    private boolean paused = false;

	public ActiveJiraIssueBean() {
        super(null, "", "");
	}


	public ActiveJiraIssueBean(final ServerId serverId, final String issueUrl, final String issueKey,
                               DateTime lastStartTime, long secondsSpent) {
		super(serverId, issueKey, issueUrl);
		this.lastStartTime = lastStartTime;
		this.secondsSpent = secondsSpent;
	}

	public ActiveJiraIssueBean(final ServerId serverId, final String issueUrl, final String issueKey, DateTime lastStartTime) {
		this(serverId, issueUrl, issueKey, lastStartTime, 0);

	}

    public ActiveJiraIssueBean(final ServerId serverId, final String issueKey, String issueUrl) {
        super(serverId, issueKey, issueUrl);
    }
   

    public void resetTimeSpent() {
		lastStartTime = new DateTime();
		secondsSpent = 0;
	}


	public long recalculateTimeSpent() {
        DateTime now = new DateTime();
        if (!paused) {
            Period nextPeriod = new Period(lastStartTime, now, PeriodType.seconds());
            secondsSpent = secondsSpent + nextPeriod.getSeconds();
        }
        lastStartTime = now;
		return secondsSpent;
	}


    public long getSecondsSpent() {
		recalculateTimeSpent();
		return this.secondsSpent;
	}

	public void setSecondsSpent(final long secondsSpent) {
		this.secondsSpent = secondsSpent;
	}


	public void setActive(final boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        recalculateTimeSpent();
        this.paused = paused;
    }

    @Transient
    public ActivationSource getSource() {
        return source;
    }
    @Transient
    public void setSource(ActivationSource source) {
        this.source = source;
    }
}
