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

package com.atlassian.theplugin.idea;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;

import java.awt.*;


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

		
		BrowserUtil.launchBrowser(BugReporting.getBugWithDescriptionUrl(ApplicationInfo.getInstance().getBuild().asString(),
                description.toString()));
		return new SubmittedReportInfo(null, "JIRA ticket", SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);
	}
}
