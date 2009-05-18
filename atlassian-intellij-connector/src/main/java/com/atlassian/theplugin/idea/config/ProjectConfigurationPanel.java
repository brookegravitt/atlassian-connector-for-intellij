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
package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.fisheye.FishEyeServerFacade;
import com.atlassian.theplugin.idea.AboutForm;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.config.serverconfig.ServerConfigPanel;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ProjectConfigurationPanel extends JPanel {
    private final FooterPanel footerPanel = new FooterPanel();
    private final JTabbedPane contentPanel = new JTabbedPane();
    private final ServerConfigPanel serverConfigPanel;
    private final ProjectDefaultsConfigurationPanel defaultsConfigurationPanel;
    private final AboutForm aboutBox;

    private Project project;
    private ProjectConfiguration projectConfiguration;
    private UserCfg defaultCredentials;
    private boolean defaultCredentialsAsked;

    private static final int WIDTH = 800;

    private static final int HEIGHT = 700;

    public ProjectConfiguration getProjectConfiguration() {
        serverConfigPanel.saveData();
        return projectConfiguration;
    }

    public UserCfg getDefaultCredentials() {
        return serverConfigPanel.getDefaultUser();
    }

    public ProjectConfigurationPanel(@NotNull final Project project,
                                     @NotNull final ProjectConfiguration projectConfiguration,
                                     @NotNull final CrucibleServerFacade crucibleServerFacade,
                                     @NotNull final FishEyeServerFacade fishEyeServerFacade,
                                     final BambooServerFacade bambooServerFacade,
                                     final JIRAServerFacade jiraServerFacade,
                                     @NotNull final UiTaskExecutor uiTaskExecutor, final ServerCfg selectedServer,
                                     /*final IntelliJProjectCfgManager projectCfgManager, */
                                     @NotNull final UserCfg defaultCredentials,
                                     final boolean defaultCredentialsAsked) {
        this.project = project;
        this.projectConfiguration = projectConfiguration;
        this.defaultCredentials = defaultCredentials;
        this.defaultCredentialsAsked = defaultCredentialsAsked;
        serverConfigPanel = new ServerConfigPanel(this, project, defaultCredentials,
                projectConfiguration, selectedServer, defaultCredentialsAsked);
        defaultsConfigurationPanel = new ProjectDefaultsConfigurationPanel(project, projectConfiguration, crucibleServerFacade,
                fishEyeServerFacade, bambooServerFacade, jiraServerFacade, uiTaskExecutor, defaultCredentials);
        aboutBox = new AboutForm();

        initLayout();
    }

    public boolean isDefaultCredentialsAsked() {
        return this.defaultCredentialsAsked;
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setPreferredSize(getMinimumSize());

        contentPanel.setOpaque(true);
        contentPanel.setBackground(new Color(Constants.BG_COLOR_R, Constants.BG_COLOR_G, Constants.BG_COLOR_B));
        contentPanel.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(final ChangeEvent e) {
                if (contentPanel.getSelectedComponent() == defaultsConfigurationPanel) {
                    defaultsConfigurationPanel.setData(projectConfiguration);
                }
            }
        });

        // add servers tab
        contentPanel.add(serverConfigPanel.getTitle(), serverConfigPanel);
        contentPanel.add("Defaults", defaultsConfigurationPanel);
        contentPanel.add("About", aboutBox.getRootPane());

        add(contentPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    public void saveData(boolean finalizeData) {
        if (finalizeData) {
            serverConfigPanel.finalizeData();
        }
        serverConfigPanel.saveData();
        if (!projectConfiguration.isDefaultFishEyeServerValid()) {
            projectConfiguration.setDefaultFishEyeServerId(null);
            Messages.showInfoMessage(this, "Default FishEye server settings have been cleared.", "Information");
        }
        if (!projectConfiguration.isDefaultCrucibleServerValid()) {
            projectConfiguration.setDefaultCrucibleServerId(null);
            Messages.showInfoMessage(this, "Default Crucible server settings have been cleared.", "Information");
        }

        if (!projectConfiguration.isDefaultJiraServerValid()) {
            projectConfiguration.setDefaultJiraServerId(null);
            Messages.showInfoMessage(this, "Default JIRA server settings have been cleared.", "Information");
        }


    }


    public void setData(ProjectConfiguration aProjectConfiguration) {
        projectConfiguration = aProjectConfiguration;
        serverConfigPanel.setData(projectConfiguration.getServers());
        defaultsConfigurationPanel.setData(projectConfiguration);
    }

    public void askForDefaultCredentials() {
        final ServerCfg serverCfg = serverConfigPanel.getSelectedServer();
        final boolean alreadyExists =
       IdeaHelper.getProjectCfgManager(project).getProjectConfiguration().getServerCfg(serverCfg.getServerId()) != null;

        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            public void run() {

                final ModalityState modalityState = ModalityState.stateForComponent(ProjectConfigurationPanel.this);
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    public void run() {

                        if (!alreadyExists && !defaultCredentialsAsked
                                && (defaultCredentials == null
                                || (defaultCredentials.getPassword().equals("")
                                && defaultCredentials.getPassword().equals(""))
                                && serverCfg.getUsername().length() > 0)) {
                            int answer = Messages.showYesNoDialog(project,
                                   "<html>Do yo want to set server <b>" + serverCfg.getName()
                                 + "</b> <i>username</i> and <i>password</i>"
                                 + " as default credentials for Atlassian IntelliJ Connector?</html>", "Set as default",
                                    Messages.getQuestionIcon());
                            if (answer == DialogWrapper.OK_EXIT_CODE) {
                                UserCfg credentials = new UserCfg(serverCfg.getUsername(),
                                        serverCfg.getPassword(), true);
                                IdeaHelper.getProjectCfgManager(project).setDefaultCredentials(credentials);
                                defaultsConfigurationPanel.setDefaultCredentials(credentials);


                            }
                            IdeaHelper.getProjectCfgManager(project).setDefaultCredentialsAsked(true);
                            ProjectConfigurationPanel.this.defaultCredentialsAsked = true;
                        }
                    }
                }, modalityState);
            }
        });
    }
}
