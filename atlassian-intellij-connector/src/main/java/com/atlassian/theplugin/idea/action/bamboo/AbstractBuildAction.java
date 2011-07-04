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
package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.connector.intellij.bamboo.IntelliJBambooServerFacade;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BuildCommentForm;
import com.atlassian.theplugin.idea.bamboo.BuildLabelForm;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public abstract class AbstractBuildAction extends AnAction {

    private void setStatusMessageUIThread(final Project project, final String message) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                setStatusMessage(project, message);
            }
        });
    }

    private void setStatusErrorMessageUIThread(final Project project, final String message) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                setStatusErrorMessage(project, message);
            }
        });
    }

    private void showExceptionDialogUIThread(final Exception e, final Project project) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                DialogWithDetails.showExceptionDialog(project, e.getMessage(), e);
            }
        });
    }

    protected abstract void setStatusMessage(final Project project, final String message);

    protected abstract void setStatusErrorMessage(final Project project, final String message);

    protected abstract BambooBuildAdapter getBuild(final AnActionEvent event);

    protected abstract List<BambooBuildAdapter> getBuilds(final AnActionEvent event);


    protected void runBuild(AnActionEvent e) {
        final Project project = IdeaHelper.getCurrentProject(e);
        final List<BambooBuildAdapter> builds = getBuilds(e);


        if (project != null) {
            for (BambooBuildAdapter build : builds) {
                if (build != null && build.getPlanKey() != null) {
                    final BambooBuildAdapter fBuild = build;
                    Task.Backgroundable executeTask
                            = new Task.Backgroundable(project, "Starting Build "  + fBuild.getPlanKey(), false) {
                        @Override
                        public void run(@NotNull final ProgressIndicator indicator) {

                            try {
                                setStatusMessageUIThread(project, "Starting build on plan: " + fBuild.getPlanKey());
                                IntelliJBambooServerFacade.getInstance(PluginUtil.getLogger()).executeBuild(
                                        fBuild.getServer(), fBuild.getPlanKey());
                                setStatusMessageUIThread(project, "Build executed on plan: " + fBuild.getPlanKey());
                            } catch (ServerPasswordNotProvidedException e) {
                                setStatusErrorMessageUIThread(project, "Build not executed: Password not provided for server");
                            } catch (final RemoteApiException e) {
                                setStatusErrorMessageUIThread(project, "Build not executed: " + e.getMessage());
                                showExceptionDialogUIThread(e, project);
                            }
                        }
                    };

                    ProgressManager.getInstance().run(executeTask);
                }
            }
        }
    }

    protected void openBuildInBrowser(final AnActionEvent e) {
        final BambooBuildAdapter build = getBuild(e);

        if (build != null) {
            BrowserUtil.launchBrowser(build.getResultUrl());
        }
    }

    protected void labelBuild(final AnActionEvent e) {
        final Project project = IdeaHelper.getCurrentProject(e);
        final List<BambooBuildAdapter> builds = getBuilds(e);

        if (project != null) {
                BuildLabelForm buildLabelForm = new BuildLabelForm(builds);
                buildLabelForm.show();
            if (buildLabelForm.getExitCode() == 0) {
                for (BambooBuildAdapter build : builds) {
                    if (build.isBamboo2() && build.areActionsAllowed()) {
                        labelBuild(project, build, buildLabelForm.getLabel());
                    }
                }
            }
        }
    }

    private void labelBuild(@NotNull final Project project, @NotNull final BambooBuildAdapter build, final String label) {

        Task.Backgroundable labelTask = new Task.Backgroundable(project, "Labeling Build", false) {
            @Override
            public void run(@NotNull final ProgressIndicator indicator) {
                setStatusMessageUIThread(project, "Applying label on build...");
                try {
                    IntelliJBambooServerFacade.getInstance(PluginUtil.getLogger()).
                            addLabelToBuild(build.getServer(), build.getPlanKey(),
                                    build.getNumber(), label);
                    setStatusMessageUIThread(project, "Label applied on build");
                } catch (ServerPasswordNotProvidedException e) {
                    setStatusErrorMessageUIThread(project, "Label not applied: Password on provided for server");
                } catch (RemoteApiException e) {
                    setStatusErrorMessageUIThread(project, "Label not applied: " + e.getMessage());
                    showExceptionDialogUIThread(e, project);
                }
            }
        };

        ProgressManager.getInstance().run(labelTask);
    }


    protected void commentBuild(AnActionEvent e) {
        final Project project = IdeaHelper.getCurrentProject(e);
        final BambooBuildAdapter build = getBuild(e);

        if (project != null && build != null) {
            BuildCommentForm buildCommentForm = new BuildCommentForm(build);
            buildCommentForm.show();
            if (buildCommentForm.getExitCode() == 0) {
                commentBuild(project, build, buildCommentForm.getCommentText());
            }
        }
    }

    private void commentBuild(@NotNull final Project project,
                              @NotNull final BambooBuildAdapter build, final String commentText) {

        Task.Backgroundable commentTask = new Task.Backgroundable(project, "Commenting Build", false) {
            @Override
            public void run(@NotNull final ProgressIndicator indicator) {
                setStatusMessageUIThread(project, "Adding comment label on build...");
                try {
                    IntelliJBambooServerFacade.getInstance(PluginUtil.getLogger()).
                            addCommentToBuild(build.getServer(), build.getPlanKey(), build.getNumber(), commentText);
                    setStatusMessageUIThread(project, "Comment added to build");
                } catch (ServerPasswordNotProvidedException e) {
                    setStatusErrorMessageUIThread(project, "Comment not added: Password not provided for server");
                } catch (RemoteApiException e) {
                    setStatusErrorMessageUIThread(project, "Comment not added: " + e.getMessage());
                    showExceptionDialogUIThread(e, project);
                }
            }
        };

        ProgressManager.getInstance().run(commentTask);
    }
}
