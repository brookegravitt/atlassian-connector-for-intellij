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

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
//import com.intellij.psi.javadoc.PsiDocToken;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: pmaruszak
 */
public final class JiraEditorLinkParser {
	private static final Pattern JIRA_ISSUE_LINK_SEARCH_PATTERN = Pattern.compile("\\b(\\p{Upper}{2,}\\-\\d+)\\b");
	private final Project project;
	private final ProjectCfgManager projectCfgManager;

	JiraEditorLinkParser(Project project, ProjectCfgManager projectCfgManager) {
		this.project = project;
		this.projectCfgManager = projectCfgManager;
	}


	private String getDefaultJiraServerUrl() {
		//projectCfgManager cannot be null but due to PL-1435 added some safety code
		// kalamon: not sure why this could be null, but see PL-1348.
		// I am too stupid to grok the project configuration code
		if (projectCfgManager != null) {
			final ServerData defaultServer = projectCfgManager.getDefaultJiraServer();
			if (defaultServer != null && projectCfgManager.isDefaultJiraServerValid()) {
				return defaultServer.getUrl();
			}
		}
		return "";
	}

	public List<JiraURLTextRange> getJiraURLTextRange(final Editor editor, int startOffset, int endOffset) {
		if (endOffset <= startOffset) {
			return Collections.emptyList();

		}

		startOffset = inBounds(editor, startOffset);
		endOffset = inBounds(editor, endOffset);
		CharSequence text;

		try {
			text = editor.getDocument().getCharsSequence().subSequence(startOffset, endOffset);
		} catch (IndexOutOfBoundsException e) {
			PluginUtil.getLogger().error(e.getMessage());
			return Collections.emptyList();
		}

		List<JiraURLTextRange> newRanges = getNewRanges(text);
		for (JiraURLTextRange range : newRanges) {
			range.shift(startOffset);
		}

		return newRanges;
	}
	//todo FIX-PHPSTORM
	public static boolean isComment(final PsiFile psiFile, final int startOffset) {
		if (psiFile == null) {
			return false;
		}
		PsiElement element = psiFile.findElementAt(startOffset);
		return element instanceof PsiComment; /*|| element instanceof PsiDocToken;*/
	}

	private List<JiraURLTextRange> getNewRanges(final CharSequence text) {
		List<JiraURLTextRange> ranges = new ArrayList<JiraURLTextRange>();
		String defaultServerurl = getDefaultJiraServerUrl();
		if (defaultServerurl.length() > 0) {
			Pattern replacePattern = Pattern.compile("$1");
			Matcher matcher = JIRA_ISSUE_LINK_SEARCH_PATTERN.matcher(text);
			while (matcher.find()) {
				String url = JIRA_ISSUE_LINK_SEARCH_PATTERN.matcher(matcher.group()).replaceAll(replacePattern.toString());
				JiraURLTextRange range = new JiraURLTextRange(project, matcher.start(), matcher.end(), url, true);
				ranges.add(range);
			}
		}

		return ranges;
	}


	public JiraURLTextRange getJiraURLTextRange(final Editor editor, final PsiFile file, final Point point) {
		if (editor == null || point == null) {
			return null;
		}

		int offset = editor.logicalPositionToOffset(editor.xyToLogicalPosition(point));
		return getJiraURLTextRange(editor, file, offset);
	}

	private JiraURLTextRange getJiraURLTextRange(final Editor editor, final PsiFile file, final int offset) {
		if (file == null) {
			return null;
		}

		int length = editor.getDocument().getTextLength();
		if (length > 0 && offset < length) {
			int startLineOffset = JiraLinkHighlighter.getStartLineOffset(editor, offset);
			int endLineOffset = JiraLinkHighlighter.getEndLineOffset(editor, offset);

			CharSequence lineText = editor.getDocument().getCharsSequence().subSequence(startLineOffset, endLineOffset);
			List<JiraURLTextRange> newRanges = getNewRanges(lineText);
			if (!newRanges.isEmpty()) {
				for (JiraURLTextRange range : newRanges) {
					if (startLineOffset + range.getStartOffset() <= offset && startLineOffset + range.getEndOffset()
							>= offset) {
						range.setActive(isComment(file, offset));
						return range;
					}
				}

			}
		}

		return null;
	}

	private static int inBounds(@NotNull Editor editor, int offset) {
		int maxValue = editor.getDocument().getTextLength();
		return Math.max(Math.min(offset, Math.max(0, maxValue)), Math.min(0, maxValue));

	}

	public JiraURLTextRange getURLTextRange(DataContext context) {
		PsiFile file = LangDataKeys.PSI_FILE.getData(context);
		Editor editor = PlatformDataKeys.EDITOR.getData(context);
		if (editor == null || editor.isDisposed()) {
			return null;
		}
		return getJiraURLTextRange(editor, file, editor.getCaretModel().getOffset());
	}
}
