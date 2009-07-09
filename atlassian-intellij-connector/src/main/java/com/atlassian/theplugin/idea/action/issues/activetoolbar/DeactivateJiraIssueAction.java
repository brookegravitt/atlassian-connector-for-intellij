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
package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.DeactivateIssueResultHandler;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class DeactivateJiraIssueAction extends AbstractActiveJiraIssueAction {
	public void actionPerformed(final AnActionEvent event) {
		runDeactivateTask(event);
	}

	public static void runDeactivateTask(final AnActionEvent event) {

		final JiraWorkspaceConfiguration conf =
				IdeaHelper.getProjectComponent(event, JiraWorkspaceConfiguration.class);

		final Project currentProject = IdeaHelper.getCurrentProject(event);

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				ActiveIssueUtils.deactivate(event, new DeactivateIssueResultHandler() {
					public void success() {
						if (conf != null) {
							conf.setActiveJiraIssuee(null);
						}
					}

					public void failure(final Throwable problem) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								if (currentProject != null && !currentProject.isDisposed()) {
									DialogWithDetails.showExceptionDialog(
											currentProject, "Failed to Deactivate Issue", problem);
								}
							}
						});
					}
				});
			}
		});
	}

	public void onUpdate(final AnActionEvent event) {
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		event.getPresentation().setEnabled(enabled);
	}
}
