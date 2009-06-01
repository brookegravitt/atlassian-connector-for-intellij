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
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.jira.DeactivateIssueResultHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.*;

/**
 * User: pmaruszak
 */
public class DeactivateJiraIssueAction extends AbstractActiveJiraIssueAction {
	public void actionPerformed(final AnActionEvent event) {
        runDeactivateTask(event);
    }

    public static void runDeactivateTask(final AnActionEvent event) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ActiveIssueUtils.deactivate(event, new DeactivateIssueResultHandler() {
                    public void success() {
                        final JiraWorkspaceConfiguration conf =
                                IdeaHelper.getProjectComponent(event, JiraWorkspaceConfiguration.class);
                        conf.setActiveJiraIssue(null);
                    }

                    public void failure(final Throwable problem) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                DialogWithDetails.showExceptionDialog(
                                        IdeaHelper.getCurrentProject(event), "Failed to Deactivate Issue", problem);
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
