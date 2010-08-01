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
package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.reviews.QuickSearchReviewAction;
import com.atlassian.theplugin.idea.crucible.ReviewListToolWindowPanel;
import com.atlassian.theplugin.idea.crucible.SearchReviewsForIssueDialog;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @autrhor pmaruszak
 * @date Jul 30, 2010
 */
public class SearchReviewsForIssueAction extends AnAction {
    @Override
    public void actionPerformed(final AnActionEvent event) {
        final Project project = DataKeys.PROJECT.getData(event.getDataContext());
        final SearchReviewsForIssueDialog dialog =
                new SearchReviewsForIssueDialog(project);
        dialog.show();
        if (dialog.isOK()) {
            final CrucibleServerFacade facade = IntelliJCrucibleServerFacade.getInstance();
            final ProjectCfgManager cfgManager = IdeaHelper.getProjectCfgManager(event);
            final ServerData defaultCrucibleServer = cfgManager.getDefaultCrucibleServer();

            ProgressManager.getInstance().run(
                    new Task.Backgroundable(project, "Searching for reviews (" + defaultCrucibleServer.getName() + ")") {
                        @Override
                        public void run(@NotNull ProgressIndicator progressIndicator) {
                            if (defaultCrucibleServer != null) {
                                try {
                                    final List<ReviewAdapter> reviews = facade.getReviewsForIssue(defaultCrucibleServer,
                                            dialog.getIssueKey());
                                    final ReviewListToolWindowPanel reviewPanel
                                            = IdeaHelper.getReviewListToolWindowPanel(project);
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            showPopup(reviews, project, event.getInputEvent().getComponent(),
                                                    dialog.getIssueKey(), reviewPanel);
                                        }
                                    });
                                } catch (RemoteApiException e) {
                                    DialogWithDetails.showExceptionDialog(project, "Error during fetching related reviews", e);
                                } catch (ServerPasswordNotProvidedException e) {
                                    DialogWithDetails.showExceptionDialog(project, "Incorrect login credentials", e);
                                }
                            }
                        }
                    }

            );
        }
    }

    private void showPopup(final List<ReviewAdapter> reviews, final Project project, final Component component,
                           final String searchIssue, final ReviewListToolWindowPanel reviewsWindow) {

        if (reviews.size() == 0) {
            Messages.showInfoMessage(project, "Reviews for issue " + searchIssue + " not found.", PluginUtil.PRODUCT_NAME);
        } else if (reviews.size() == 1) {
            reviewsWindow.openReview(reviews.iterator().next(), true);
        } else if (reviews.size() > 1) {
            final ListPopup popup = JBPopupFactory.getInstance().createListPopup(
                    new QuickSearchReviewAction.ReviewListPopupStep(
                            "Found " + reviews.size() + " reviews", reviews, reviewsWindow));
            popup.show(component);

        }
    }
}
