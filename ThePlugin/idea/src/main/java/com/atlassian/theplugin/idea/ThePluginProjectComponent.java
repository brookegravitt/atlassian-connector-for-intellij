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

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.UIActionScheduler;
import com.atlassian.theplugin.commons.bamboo.*;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.ConfigurationListener;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.configuration.CrucibleTooltipOption;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.autoupdate.ConfirmPluginUpdateHandler;
import com.atlassian.theplugin.idea.autoupdate.PluginUpdateIcon;
import com.atlassian.theplugin.idea.bamboo.*;
import com.atlassian.theplugin.idea.crucible.CruciblePatchSubmitExecutor;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusIcon;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.notification.crucible.CrucibleNotificationTooltip;
import com.atlassian.theplugin.notification.crucible.CrucibleReviewNotifier;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandler;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Per-project plugin component.
 */
@State(name = "atlassian-ide-plugin-workspace",
		storages = {@Storage(id = "atlassian-ide-plugin-workspace-id", file = "$WORKSPACE_FILE$") })
public class ThePluginProjectComponent implements ProjectComponent, PersistentStateComponent<ProjectConfigurationBean> {
    private static final String THE_PLUGIN_TOOL_WINDOW_ICON = "/icons/ico_plugin_16.png";

    private final ProjectConfigurationBean projectConfigurationBean;

    private final Project project;

	public CfgManager getCfgManager() {
		return cfgManager;
	}

	private final CfgManager cfgManager;
    private final TestResultsToolWindow testResultsToolWindow;
    private final BuildChangesToolWindow buildChangesToolWindow;
    private final UIActionScheduler actionScheduler;
	private BambooStatusIcon statusBarBambooIcon;

    private CrucibleStatusIcon statusBarCrucibleIcon;
    private PluginUpdateIcon statusPluginUpdateIcon;
    private BambooStatusChecker bambooStatusChecker;
	private CrucibleStatusChecker crucibleStatusChecker;

	private BambooStatusTooltipListener tooltipBambooStatusListener;
	private BambooTableToolWindowPanel bambooToolWindowPanel;
    private CrucibleTableToolWindowPanel crucibleToolWindowPanel;

    private final CrucibleServerFacade crucibleServerFacade;

    private final ToolWindowManager toolWindowManager;
    private boolean created;
    private CrucibleReviewNotifier crucibleReviewNotifier;

	private final PluginConfiguration pluginConfiguration;

    private JIRAToolWindowPanel jiraToolWindowPanel;
    private JIRAServer currentJiraServer;

	private PluginToolWindow toolWindow;

//	public static final Key<ReviewActionEventBroker> BROKER_KEY = Key.create("thePlugin.broker");
	private ConfigurationListenerImpl configurationListener;

	public ThePluginProjectComponent(Project project, ToolWindowManager toolWindowManager,
			PluginConfiguration pluginConfiguration, UIActionScheduler actionScheduler,
			ProjectConfigurationBean projectConfigurationBean, CfgManager cfgManager,
            TestResultsToolWindow testResultsToolWindow,
			BuildChangesToolWindow buildChangesToolWindow) {
		this.project = project;
		this.cfgManager = cfgManager;
//        project.putUserData(BROKER_KEY, new ReviewActionEventBroker(project));

		this.actionScheduler = actionScheduler;
		this.toolWindowManager = toolWindowManager;
		this.pluginConfiguration = pluginConfiguration;
		this.projectConfigurationBean = projectConfigurationBean;
		this.crucibleServerFacade = CrucibleServerFacadeImpl.getInstance();
        this.testResultsToolWindow = testResultsToolWindow;
        this.buildChangesToolWindow = buildChangesToolWindow;
		/*


										WARNING!!!


		BEFORE ADDING SOME INITIALIZATION CODE TO COSTRUCTOR THINK TWICE
                                         st
		...MAYBE YOU SHOULD PUT IT INTO THE initializePlugin METHOD
		(WHICH IS INVOKED WHEN THE ENTIRE PLUGIN ENVIRONMENT IS SET UP)?


		 */
		// make findBugs happy
        statusBarBambooIcon = null;
        statusBarCrucibleIcon = null;
        statusPluginUpdateIcon = null;
        created = false;
        StartupManager.getInstance(project).registerPostStartupActivity(new Runnable() {
            public void run() {
				LoggerImpl.getInstance().info("Start: Project initializing");
                initializePlugin();
                LoggerImpl.getInstance().info("End: Project initialized");
            }
        });
	}

    public void initComponent() {
        LoggerImpl.getInstance().info("Init ThePlugin project component.");
    }

    public void disposeComponent() {
        LoggerImpl.getInstance().info("Dispose ThePlugin project component");
    }

    @NotNull
	public String getComponentName() {
        return "ThePluginProjectComponent";
    }

	private void initializePlugin() {
        // unregister changelistmanager?
        // only open tool windows for each application that's registered
        // show something nice if there are non
        // swap listener for dataretrievedlistener and datachangelisteners
        // store bamboo between runs in UDC
        // clean up object model confusion

        if (!created) {

            ChangeListManager.getInstance(project).registerCommitExecutor(
                    new CruciblePatchSubmitExecutor(project, crucibleServerFacade, cfgManager));

            this.bambooToolWindowPanel = new BambooTableToolWindowPanel(
                    project, projectConfigurationBean, testResultsToolWindow, buildChangesToolWindow);

			// wseliga: I don't know yet what do to with comment below
			// todo remove that get instance as it can return null. it is better to get it from app component.
			this.bambooStatusChecker = new BambooStatusChecker(CfgUtil.getProjectId(project), actionScheduler,
					cfgManager, pluginConfiguration,
					new MissingPasswordHandler(BambooServerFacadeImpl.getInstance(PluginUtil.getLogger()), cfgManager, project),
					PluginUtil.getLogger());

			this.crucibleStatusChecker = new CrucibleStatusChecker(cfgManager, project,
					pluginConfiguration.getCrucibleConfigurationData(), projectConfigurationBean.getCrucibleConfiguration(),
					new MissingPasswordHandler(crucibleServerFacade, cfgManager, project));

			// DependencyValidationManager.getHolder(project, "", )
			//this.bambooToolWindowPanel = BambooTableToolWindowPanel.getInstance(project, projectConfigurationBean);
			this.crucibleToolWindowPanel = new CrucibleTableToolWindowPanel(project,
					projectConfigurationBean, crucibleStatusChecker);
			this.jiraToolWindowPanel = JIRAToolWindowPanel.getInstance(project, projectConfigurationBean, cfgManager);

			// create tool window on the right
            toolWindow = new PluginToolWindow(toolWindowManager, project, cfgManager);
            Icon toolWindowIcon = IconLoader.getIcon(THE_PLUGIN_TOOL_WINDOW_ICON);
            toolWindow.getIdeaToolWindow().setIcon(toolWindowIcon);

            // create tool window content

            toolWindow.registerPanel(PluginToolWindow.ToolWindowPanels.BAMBOO);
            TableView.restore(projectConfigurationBean.getBambooConfiguration().getTableConfiguration(),
                    bambooToolWindowPanel.getTable());


            toolWindow.registerPanel(PluginToolWindow.ToolWindowPanels.CRUCIBLE);


            toolWindow.registerPanel(PluginToolWindow.ToolWindowPanels.JIRA);

			TableView.restore(projectConfigurationBean.getJiraConfiguration().getTableConfiguration(),
                    jiraToolWindowPanel.getTable());

			IdeaHelper.getAppComponent().getSchedulableCheckers().add(bambooStatusChecker);
            // add tool window bamboo content listener to bamboo checker thread
            bambooStatusChecker.registerListener(bambooToolWindowPanel);

			// create Bamboo status bar icon
            statusBarBambooIcon = new BambooStatusIcon(this.project, cfgManager);
            statusBarBambooIcon.updateBambooStatus(BuildStatus.UNKNOWN, new BambooPopupInfo());

            // add icon listener to bamboo checker thread
			final StausIconBambooListener iconBambooStatusListener = new StausIconBambooListener(statusBarBambooIcon);
            bambooStatusChecker.registerListener(iconBambooStatusListener);

            // add simple bamboo listener to bamboo checker thread
            // this listener shows idea tooltip when buld failed
            final BambooStatusDisplay bambooStatusDisplay = new BuildStatusChangedToolTip(project);
            tooltipBambooStatusListener = new BambooStatusTooltipListener(bambooStatusDisplay, pluginConfiguration);
            bambooStatusChecker.registerListener(tooltipBambooStatusListener);

            // add bamboo icon to status bar
            statusBarBambooIcon.showOrHideIcon();

            // setup Crucible status checker and listeners
			IdeaHelper.getAppComponent().getSchedulableCheckers().add(crucibleStatusChecker);
            crucibleStatusChecker.registerListener(crucibleToolWindowPanel);
            // create crucible status bar icon
            statusBarCrucibleIcon = new CrucibleStatusIcon(project, cfgManager);

			registerCrucibleNotifier();

			// add crucible icon to status bar
            //statusBar.addCustomIndicationComponent(statusBarCrucibleIcon);
            statusBarCrucibleIcon.showOrHideIcon();

            statusPluginUpdateIcon = new PluginUpdateIcon(project, pluginConfiguration, cfgManager);
            ConfirmPluginUpdateHandler.getInstance().setDisplay(statusPluginUpdateIcon);
            //statusPluginUpdateIcon.showOrHideIcon();

            toolWindow.showHidePanels();
            // focus last active panel only if it exists (do not create panel)
            PluginToolWindow.focusPanelIfExists(project, projectConfigurationBean.getActiveToolWindowTab());

            IdeaHelper.getAppComponent().rescheduleStatusCheckers(false);

			jiraToolWindowPanel.selectLastActiveServer();

			configurationListener = new ConfigurationListenerImpl();
			cfgManager.addProjectConfigurationListener(CfgUtil.getProjectId(project), configurationListener);

			created = true;
        }
    }

	public CrucibleReviewNotifier getCrucibleReviewNotifier() {
		return crucibleReviewNotifier;
	}

	public void registerCrucibleNotifier() {
		if (crucibleReviewNotifier == null) {
			crucibleReviewNotifier = new CrucibleReviewNotifier(project);
		}

		if (pluginConfiguration.getCrucibleConfigurationData().getCrucibleTooltipOption()
				!= CrucibleTooltipOption.NEVER) {

			if (!crucibleStatusChecker.getListenerList().contains(crucibleReviewNotifier)) {
				final CrucibleNotificationTooltip crucibleNotificationTooltip = new CrucibleNotificationTooltip(
					statusBarCrucibleIcon, project);

				crucibleReviewNotifier.registerListener(crucibleNotificationTooltip);
				crucibleStatusChecker.registerListener(crucibleReviewNotifier);
			}

		} else {
			crucibleStatusChecker.unregisterListener(crucibleReviewNotifier);
		}
	}

	public Content createBambooContent() {
        PeerFactory peerFactory = PeerFactory.getInstance();

        Content content = peerFactory.getContentFactory().createContent(
                bambooToolWindowPanel,
                PluginToolWindow.ToolWindowPanels.BAMBOO.toString(),
                false);

        content.setIcon(IconLoader.getIcon("/icons/tab_bamboo.png"));
        content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);

        return content;
    }


	public Content createCrucibleContent() {
        PeerFactory peerFactory = PeerFactory.getInstance();

        Content content = peerFactory.getContentFactory().createContent(
                crucibleToolWindowPanel, PluginToolWindow.ToolWindowPanels.CRUCIBLE.toString(), false);
        content.setIcon(IconLoader.getIcon("/icons/tab_crucible.png"));
        content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);

        return content;
    }


    public Content createJiraContent() {
        PeerFactory peerFactory = PeerFactory.getInstance();

        Content content = peerFactory.getContentFactory().createContent(
                jiraToolWindowPanel, PluginToolWindow.ToolWindowPanels.JIRA.toString(), false);
        content.setIcon(IconLoader.getIcon("/icons/tab_jira.png"));
        content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);

        return content;
    }

	public void projectOpened() {
        // content moved to StartupManager to wait until
		// here we have guarantee that IDEA splash screen will not obstruct our window
		askForUserStatistics();
	}

	private void askForUserStatistics() {
		if (pluginConfiguration.getGeneralConfigurationData().getAnonymousFeedbackEnabled() == null) {
			int answer = Messages.showYesNoDialog("We would greatly appreciate if you allow us to collect anonymous "
					+ "usage statistics to help us provide a better quality product. Is this ok?",
					PluginUtil.getInstance().getName() + " request", Messages.getQuestionIcon());
			pluginConfiguration.getGeneralConfigurationData().setAnonymousFeedbackEnabled(answer == DialogWrapper.OK_EXIT_CODE);
		}
	}

	public void projectClosed() {
		if (created) {
			// remove icon from status bar
			statusBarBambooIcon.hideIcon();
			statusBarBambooIcon = null;
			statusBarCrucibleIcon.hideIcon();
			statusBarCrucibleIcon = null;
			statusPluginUpdateIcon.hideIcon();
			statusPluginUpdateIcon = null;

			IdeaHelper.getAppComponent().getSchedulableCheckers().remove(bambooStatusChecker);
			IdeaHelper.getAppComponent().getSchedulableCheckers().remove(crucibleStatusChecker);
			IdeaHelper.getAppComponent().rescheduleStatusCheckers(true);
			// unregister listeners
			//bambooStatusChecker.unregisterListener(iconBambooStatusListener);
			//bambooStatusChecker.unregisterListener(toolWindowBambooListener);
			bambooStatusChecker.unregisterListener(tooltipBambooStatusListener);
			crucibleStatusChecker.unregisterListener(crucibleToolWindowPanel);
			crucibleStatusChecker.unregisterListener(crucibleReviewNotifier);
			cfgManager.removeProjectConfigurationListener(CfgUtil.getProjectId(project), configurationListener);

			// remove tool window
			toolWindowManager.unregisterToolWindow(PluginToolWindow.TOOL_WINDOW_NAME);

			created = false;
		}
	}

    public ProjectConfigurationBean getState() {
        return projectConfigurationBean;
    }



	public void loadState(ProjectConfigurationBean state) {
        projectConfigurationBean.copyConfiguration(state);
	}

    public ProjectConfigurationBean getProjectConfigurationBean() {
        return projectConfigurationBean;
    }

    public JIRAServer getCurrentJiraServer() {
        return currentJiraServer;
    }

    public void setCurrentJiraServer(JIRAServer currentJiraServer) {
        this.currentJiraServer = currentJiraServer;
    }

    public CrucibleStatusChecker getCrucibleStatusChecker() {
        return crucibleStatusChecker;
    }


	public BambooStatusChecker getBambooStatusChecker() {
        return bambooStatusChecker;
    }

	private class ConfigurationListenerImpl implements ConfigurationListener {
		public void configurationUpdated(final ProjectConfiguration aProjectConfiguration) {
			// show-hide icons if necessary
			statusBarBambooIcon.showOrHideIcon();
			statusBarCrucibleIcon.showOrHideIcon();
			// show-hide panels if necessary
			toolWindow.showHidePanels();
		}

		public void projectUnregistered() {
		}
	}
}
