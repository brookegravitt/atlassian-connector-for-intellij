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
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.jira.model.JIRAServerModelIdea;
import com.atlassian.theplugin.util.PluginUtil;
import com.google.common.base.*;
import com.google.common.collect.Collections2;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: pmaruszak
 */
public final class JiraEditorLinkParser {
    // adding numbers seems a "fairly" common pattern
    private static final String JIRA_ISSUE_LINK_SEARCH_PATTERN = "\\b(([\\p{Upper}]{2,})\\-\\d+)\\b";

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

	public static boolean isComment(final PsiFile psiFile, final int startOffset) {
		if (psiFile == null) {
			return false;
		}
		PsiElement element = psiFile.findElementAt(startOffset);
		return element instanceof PsiComment || IdeaVersionFacade.isInstanceOfPsiDocToken(element);
	}


	private List<JiraURLTextRange> getNewRanges(final CharSequence text) {
		List<JiraURLTextRange> ranges = new ArrayList<JiraURLTextRange>();
		String defaultServerurl = getDefaultJiraServerUrl();
		if (defaultServerurl.length() > 0) {
            List<String> jiraProjectKeys = getJiraProjectKeys();

            String regex = createIssueKeyRegex(jiraProjectKeys);
            Pattern pattern = Pattern.compile(regex);

			Pattern replacePattern = Pattern.compile("$1");
			Matcher matcher = pattern.matcher(text);

            while (matcher.find()) {
                String url = pattern.matcher(matcher.group()).replaceAll(replacePattern.toString());
                JiraURLTextRange range = new JiraURLTextRange(project, matcher.start(), matcher.end(), url, true);
                ranges.add(range);
			}
		}

		return ranges;
	}

    private String createIssueKeyRegex(List<String> jiraProjectKeys) {
        // fall back to default pattern if no keys are available - e.g. if they have not yet been loaded
        if (jiraProjectKeys == null || jiraProjectKeys.size() == 0) {
            return JIRA_ISSUE_LINK_SEARCH_PATTERN;
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("\\b((?:");
            builder.append(Joiner.on("|").join(jiraProjectKeys));
            builder.append(")-\\d+)\\b");

            return builder.toString();
        }
    }

    private List<String> getJiraProjectKeys() {
        JIRAServerModelIdea jiraServerModel = (JIRAServerModelIdea) IdeaHelper.getJIRAServerModel(project);
        List<String> jiraProjectKeys = new ArrayList<String>();
        try {
            Collection<JiraServerData> servers = projectCfgManager.getAllEnabledJiraServerss();

            for (JiraServerData jiraServer: servers) {
                // we need to get projects from the cache only, as all of this is happening in the event thread
                Collection<JIRAProject> jiraProjects = com.google.common.base.Objects.firstNonNull(jiraServerModel.getProjectsFromCache(jiraServer), new ArrayList<JIRAProject>());
                jiraProjects = Collections2.filter(jiraProjects, new Predicate<JIRAProject>() {
                    public boolean apply(JIRAProject jiraProject) {
                        return jiraProject.getKey() != null;
                    }
                });
                jiraProjectKeys.addAll(Collections2.transform(jiraProjects, new Function<JIRAProject, String>() {
                    public String apply(JIRAProject jiraProject) {
                        return jiraProject.getKey();
                    }
                }));
            }
        } catch (JIRAException e) {
            // meh, IDEA catches and logs those anyway
            throw new RuntimeException(e);
        }
        return jiraProjectKeys;
    }


    public JiraURLTextRange getJiraURLTextRange(final Editor editor, final PsiFile file, final Point point) {
		if (editor == null || point == null) {
			return null;
		}

        // PL-2671 - WTF is Throwable thrown? Anybody?
        try {
            int offset = editor.logicalPositionToOffset(editor.xyToLogicalPosition(point));
            return getJiraURLTextRange(editor, file, offset);
        } catch (Throwable t) {
            return null;
        }
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
		Editor editor = LangDataKeys.EDITOR.getData(context);
		if (editor == null || editor.isDisposed()) {
			return null;
		}
		return getJiraURLTextRange(editor, file, editor.getCaretModel().getOffset());
	}
}
