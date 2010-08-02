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
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.SvnRepository;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.action.reviews.QuickSearchReviewAction;
import com.atlassian.theplugin.idea.crucible.ReviewListToolWindowPanel;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * @autrhor pmaruszak
 * @date Jul 21, 2010
 */
public class ShowAllReviewsForFileAction extends AnAction {
    @Override
    public void actionPerformed(final AnActionEvent anActionEvent) {
        final Project project = DataKeys.PROJECT.getData(anActionEvent.getDataContext());
        final FileEditorManager fileEditorManager = FileEditorManagerImpl.getInstance(project);


        ProgressManager.getInstance().run(
                new Task.Backgroundable(project, "Searching for reviews") {
                    @Override
                    public void run(@NotNull ProgressIndicator progressIndicator) {
                        final ReviewListToolWindowPanel panel = IdeaHelper.getReviewListToolWindowPanel(anActionEvent);
                        final ProjectCfgManager cfgManager = IdeaHelper.getProjectCfgManager(anActionEvent);
                        ServerData selectedServer;
                        String selectedRepoName;


                        selectedServer = cfgManager.getDefaultCrucibleServer();
                        selectedRepoName = cfgManager.getDefaultCrucibleRepo();

                        IntelliJCrucibleServerFacade facade = IntelliJCrucibleServerFacade.getInstance();

                        VirtualFile[] selectedFiles = fileEditorManager.getSelectedFiles();
                        if (isContextValid(anActionEvent)) {
                            try {
                                SvnRepository repo = (SvnRepository) facade.getRepository(selectedServer, selectedRepoName);
                                final String filePath = getSVNPathForFile(VcsIdeaHelper.getRepositoryUrlForFile(project,
                                        selectedFiles[0]), repo, project);
                                final List<ReviewAdapter> reviews = facade.getAllReviewsForFile(selectedServer,
                                        selectedRepoName, filePath);
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        showPopup(reviews, project, fileEditorManager.getSelectedTextEditor(),
                                                filePath, panel);
                                    }
                                }
                                );
                            } catch (RemoteApiException e) {
                                DialogWithDetails.showExceptionDialog(project, "Error occured", e);
                            } catch (ServerPasswordNotProvidedException
                                    e) {
                                DialogWithDetails.showExceptionDialog(project, "Incorrect password", e);
                            }
                        }

                    }
                }

        );


    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(isContextValid(e));
    }

    private boolean isContextValid(final AnActionEvent e) {
        ProjectCfgManager projectCfgManager = IdeaHelper.getProjectCfgManager(e);
        return projectCfgManager.getDefaultCrucibleServer() != null && projectCfgManager.getDefaultCrucibleRepo() != null;
    }

    private String getSVNPathForFile(String filePath, SvnRepository repo, final Project project) {
        if (repo != null && filePath != null && repo.getUrl() != null && filePath.contains(repo.getUrl())) {
            filePath = filePath.replace(repo.getUrl(), "");
        }

        if (filePath != null && filePath.startsWith(repo.getPath())) {
            //removes also "/" slash
            filePath = filePath.substring(repo.getPath().length() + 1);
        }
        return filePath;

    }

    private void showPopup(final List<ReviewAdapter> reviews, final Project project, final Editor editor,
                           final String searchFile, final ReviewListToolWindowPanel reviewsWindow) {

        if (reviews.size() == 0) {
            Messages.showInfoMessage(project, "Reviews for a file " + searchFile + " not found.", PluginUtil.PRODUCT_NAME);
        } else if (reviews.size() == 1) {
            reviewsWindow.openReview(reviews.iterator().next(), true);
        } else if (reviews.size() > 1) {
            final ListPopup popup = JBPopupFactory.getInstance().createListPopup(
                    new QuickSearchReviewAction.ReviewListPopupStep(
                            "Found " + reviews.size() + " reviews", reviews, reviewsWindow));
            popup.showInBestPositionFor(editor);

        }
    }
}

