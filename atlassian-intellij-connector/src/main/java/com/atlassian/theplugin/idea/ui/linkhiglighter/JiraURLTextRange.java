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
package com.atlassian.theplugin.idea.ui.linkhiglighter;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * User: pmaruszak
 */
public class JiraURLTextRange {
	public static final Color LINK_COLOR = Color.BLUE;
	public static final TextAttributes ACTIVE_ISSUE_LINK_TEXT_ATTRIBUTES =
			new TextAttributes(LINK_COLOR, null, LINK_COLOR, EffectType.LINE_UNDERSCORE, Font.PLAIN);

	private final Project project;
	private int startOffset;
	private int endOffset;

	private final String issueKey;

	private boolean active;

	private int hash = 0;

	private RangeHighlighter rangeHighlighter = null;

	private static final Key<JiraURLTextRange> JIRA_ISSUE_LINK_HIGHLIGHTER_KEY = Key.create("JiraIssueLinkHighlighter");

	public JiraURLTextRange(@NotNull final Project project,
			final int startOffset, final int endOffset, final String issueKey, final boolean isActive) {
		this.project = project;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.issueKey = issueKey;
		active = isActive;
	}

	private String getDefaultJiraServerUrl() {
		final ProjectCfgManagerImpl projectCfgManager = IdeaHelper.getProjectCfgManager(project);
		if (projectCfgManager != null) {
			final ServerData defaultServer = projectCfgManager.getDefaultJiraServer();
			if (defaultServer != null && projectCfgManager.isDefaultJiraServerValid()) {
				return defaultServer.getUrl();
			}
		}
		return "";
	}

	public String getBrowserUrl() {
		return getDefaultJiraServerUrl() + "/browse/" + issueKey;
	}

	public String getIssueKey() {
		return issueKey;
	}

	public int getStartOffset() {
		return startOffset;
	}

	public int getEndOffset() {
		return endOffset;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}


	public boolean isActive() {
		return active;
	}

	public void addLinkHighlighter(final Editor editor) {
		MarkupModel markupModel = editor.getMarkupModel();
		if (rangeHighlighter != null) {
			markupModel.removeHighlighter(rangeHighlighter);
		}
		rangeHighlighter = markupModel.addRangeHighlighter(this.getStartOffset(), this.getEndOffset(),
				HighlighterLayer.WARNING - 1, ACTIVE_ISSUE_LINK_TEXT_ATTRIBUTES, HighlighterTargetArea.EXACT_RANGE);
		rangeHighlighter.setErrorStripeMarkColor(LINK_COLOR);
		rangeHighlighter.setErrorStripeTooltip("issue " + issueKey + " on " + getDefaultJiraServerUrl());
		rangeHighlighter.putUserData(JIRA_ISSUE_LINK_HIGHLIGHTER_KEY, this);
//        rangeHighlighter.setGutterIconRenderer(new JiraLinkGutterRenderer());
	}

//    private class JiraLinkGutterRenderer extends GutterIconRenderer {
//
//        @NotNull
//        public Icon getIcon() {
//            return Constants.JIRA_ISSUE_PANEL_ICON;
//        }
//
//        @Override
//        public boolean isNavigateAction() {
//            return true;
//        }
//
//        @Override
//        public String getTooltipText() {
//            return issueKey;
//        }
//    }

	public void removeLinkHighlighter(final Editor editor) {
		MarkupModel markupModel = editor.getMarkupModel();
		if (rangeHighlighter != null) {
			markupModel.removeHighlighter(rangeHighlighter);
			rangeHighlighter = null;
		}

	}

	public static JiraURLTextRange getFrom(final RangeHighlighter rangeHighlighter) {
		return rangeHighlighter.getUserData(JIRA_ISSUE_LINK_HIGHLIGHTER_KEY);
	}

	public void shift(final int shiftOffset) {
		startOffset += shiftOffset;
		endOffset += shiftOffset;
		hash += 2 * shiftOffset;
	}

	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof JiraURLTextRange)) {
			return false;
		}

		final JiraURLTextRange that = (JiraURLTextRange) o;

		if (endOffset != that.endOffset) {
			return false;
		}
		if (startOffset != that.startOffset) {
			return false;
		}
		if (!issueKey.equals(that.issueKey)) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		if (hash == 0) {
			result = startOffset;
			result = 31 * result + endOffset;
			result = 31 * result + issueKey.hashCode();
			hash = result;
		}
		return hash;
	}
}
