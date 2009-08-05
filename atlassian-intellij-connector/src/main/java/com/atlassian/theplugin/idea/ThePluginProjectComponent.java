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

import com.atlassian.connector.intellij.bamboo.BambooPopupInfo;
import com.atlassian.connector.intellij.bamboo.BambooStatusChecker;
import com.atlassian.connector.intellij.bamboo.BambooStatusDisplay;
import com.atlassian.connector.intellij.bamboo.BambooStatusListener;
import com.atlassian.connector.intellij.bamboo.BambooStatusTooltipListener;
import com.atlassian.connector.intellij.bamboo.StatusIconBambooListener;
import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.theplugin.commons.UIActionScheduler;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.configuration.WorkspaceConfigurationBean;
import com.atlassian.theplugin.crucible.model.CrucibleReviewListModel;
import com.atlassian.theplugin.idea.autoupdate.ConfirmPluginUpdateHandler;
import com.atlassian.theplugin.idea.autoupdate.PluginUpdateIcon;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.idea.bamboo.BambooStatusIcon;
import com.atlassian.theplugin.idea.bamboo.BuildListModelImpl;
import com.atlassian.theplugin.idea.bamboo.BuildStatusChangedToolTip;
import com.atlassian.theplugin.idea.config.MissingPasswordHandler;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.idea.crucible.CruciblePatchSubmitExecutor;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusIcon;
import com.atlassian.theplugin.idea.crucible.editor.CrucibleEditorFactoryListener;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.atlassian.theplugin.idea.ui.InformationDialogWithCheckBox;
import com.atlassian.theplugin.idea.ui.linkhiglighter.FileEditorListenerImpl;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.notification.crucible.CrucibleNotificationTooltip;
import com.atlassian.theplugin.notification.crucible.CrucibleReviewNotifier;
import com.atlassian.theplugin.remoteapi.MissingPasswordHandlerQueue;
import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.util.UsageStatisticsGenerator;
import org.jetbrains.annotations.NotNull;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.Collection;

/**
 * Per-project plugin component.
 */

public class ThePluginProjectComponent implements ProjectComponent {
	private static final String THE_PLUGIN_TOOL_WINDOW_ICON = "/icons/ico_plugin_16.png";

	private final WorkspaceConfigurationBean projectConfigurationBean;
	private final Project project;

	public ProjectCfgManagerImpl getCfgManager() {
		return projectCfgManager;
	}

	private final ProjectCfgManagerImpl projectCfgManager;
	private final UIActionScheduler actionScheduler;
	private BambooStatusIcon statusBarBambooIcon;
	private CrucibleStatusIcon statusBarCrucibleIcon;

	private CrucibleNotificationTooltip crucibleTooltip;
	private PluginUpdateIcon statusPluginUpdateIcon;
	private BambooStatusChecker bambooStatusChecker;
	private final BuildListModelImpl bambooModel;
	private final CrucibleStatusChecker crucibleStatusChecker;

	private BambooStatusTooltipListener tooltipBambooStatusListener;

	private final CrucibleServerFacade crucibleServerFacade;

	private final ToolWindowManager toolWindowManager;
	private boolean created;
	private final CrucibleReviewNotifier crucibleReviewNotifier;
	private final CrucibleReviewListModel crucibleReviewListModel;
	private final JIRAIssueListModelBuilder jiraIssueListModelBuilder;
	private final PluginConfiguration pluginConfiguration;

	private final IssueListToolWindowPanel issuesToolWindowPanel;

	private final PluginToolWindow toolWindow;

	//	public static final Key<ReviewActionEventBroker> BROKER_KEY = Key.create("thePlugin.broker");
	private ConfigurationListenerImpl configurationListener;

	private CrucibleEditorFactoryListener crucibleEditorFactoryListener;

	private FileEditorListenerImpl fileEditorListener;

	public ThePluginProjectComponent(Project project, ToolWindowManager toolWindowManager,
			PluginConfiguration pluginConfiguration, UIActionScheduler actionScheduler,
			WorkspaceConfigurationBean projectConfigurationBean,
			@NotNull IssueListToolWindowPanel issuesToolWindowPanel,
			@NotNull PluginToolWindow pluginToolWindow,
			@NotNull BuildListModelImpl bambooModel,
			@NotNull final CrucibleStatusChecker crucibleStatusChecker,
			@NotNull final CrucibleReviewNotifier crucibleReviewNotifier,
			@NotNull final CrucibleReviewListModel crucibleReviewListModel,
			@NotNull final ProjectCfgManagerImpl projectCfgManager,
			@NotNull final JIRAIssueListModelBuilder jiraIssueListModelBuilder) {
		this.project = project;
		this.projectCfgManager = projectCfgManager;
		this.jiraIssueListModelBuilder = jiraIssueListModelBuilder;
		this.actionScheduler = actionScheduler;
		this.toolWindowManager = toolWindowManager;
		this.pluginConfiguration = pluginConfiguration;
		this.projectConfigurationBean = projectConfigurationBean;
		this.bambooModel = bambooModel;
		this.crucibleStatusChecker = crucibleStatusChecker;
		this.crucibleReviewNotifier = crucibleReviewNotifier;
		this.crucibleReviewListModel = crucibleReviewListModel;
		this.crucibleServerFacade = IntelliJCrucibleServerFacade.getInstance();
		this.issuesToolWindowPanel = issuesToolWindowPanel;
		this.toolWindow = pluginToolWindow;

		jiraIssueListModelBuilder.setProject(project);
//		jiraIssueListModelBuilder.setProjectCfgManager(projectCfgManager);
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
		this.fileEditorListener = new FileEditorListenerImpl(project, projectCfgManager);
		//ActivateJiraIssueAction.showToolbar(project);
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

			toolWindow.register(toolWindowManager);

			ChangeListManager.getInstance(project).registerCommitExecutor(
					new CruciblePatchSubmitExecutor(project, crucibleServerFacade, projectCfgManager));

			final MissingPasswordHandler pwdHandler = new MissingPasswordHandler(
					BambooServerFacadeImpl.getInstance(PluginUtil.getLogger()),
					projectCfgManager,
					project);

			this.bambooStatusChecker = new BambooStatusChecker(
					actionScheduler,
					projectCfgManager,
					pluginConfiguration,
					new Runnable() {
						public void run() {
							MissingPasswordHandlerQueue.addHandler(pwdHandler);
						}
					},
					PluginUtil.getLogger());

			// DependencyValidationManager.getHolder(project, "", )

			issuesToolWindowPanel.refreshModels();

			// create Atlassian tool window
//			toolWindow = new PluginToolWindow(toolWindowManager, project, cfgManager, bambooToolWindowPanel);
			Icon toolWindowIcon = IconLoader.getIcon(THE_PLUGIN_TOOL_WINDOW_ICON);
			toolWindow.getIdeaToolWindow().setIcon(toolWindowIcon);

			// create tool window content

//			toolWindow.registerPanel(PluginToolWindow.ToolWindowPanels.BAMBOO_OLD);

			toolWindow.registerPanel(PluginToolWindow.ToolWindowPanels.BUILDS);
			toolWindow.registerPanel(PluginToolWindow.ToolWindowPanels.CRUCIBLE);
			toolWindow.registerPanel(PluginToolWindow.ToolWindowPanels.ISSUES);

			IdeaHelper.getAppComponent().getSchedulableCheckers().add(bambooStatusChecker);
			// add tool window bamboo content listener to bamboo checker thread
			bambooStatusChecker.registerListener(new BambooStatusListener() {
				
				
				public void updateBuildStatuses(Collection<BambooBuildAdapterIdea> builds, Collection<Exception> generalExceptions) {
					bambooModel.update(builds, generalExceptions);
				}

				public void resetState() {
				}
			});

			// create Bamboo status bar icon
			statusBarBambooIcon = new BambooStatusIcon(this.project, projectCfgManager, toolWindow);
			statusBarBambooIcon.updateBambooStatus(BuildStatus.UNKNOWN, new BambooPopupInfo());

			// add icon listener to bamboo checker thread
			final StatusIconBambooListener iconBambooStatusListener = new StatusIconBambooListener(statusBarBambooIcon);
			bambooStatusChecker.registerListener(iconBambooStatusListener);

			// add simple bamboo listener to bamboo checker thread
			// this listener shows idea tooltip when buld failed
			final BambooStatusDisplay bambooStatusDisplay = new BuildStatusChangedToolTip(project, toolWindow);
			tooltipBambooStatusListener = new BambooStatusTooltipListener(bambooStatusDisplay, pluginConfiguration);
			bambooStatusChecker.registerListener(tooltipBambooStatusListener);
//			bambooStatusChecker.registerListener(buildToolWindowPanel.getBuildTree());

			// add bamboo icon to status bar
			statusBarBambooIcon.showOrHideIcon();

			// setup Crucible status checker and listeners
			IdeaHelper.getAppComponent().getSchedulableCheckers().add(crucibleStatusChecker);
			// create crucible status bar icon
			statusBarCrucibleIcon = new CrucibleStatusIcon(project, projectCfgManager, toolWindow);

			//registerCrucibleNotifier();

			// add crucible icon to status bar
			//statusBar.addCustomIndicationComponent(statusBarCrucibleIcon);
			statusBarCrucibleIcon.showOrHideIcon();

			statusPluginUpdateIcon = new PluginUpdateIcon(project, pluginConfiguration, projectCfgManager);
			ConfirmPluginUpdateHandler.getInstance().setDisplay(statusPluginUpdateIcon);
			//statusPluginUpdateIcon.showOrHideIcon();

			toolWindow.showHidePanels();
			// focus last active panel only if it exists (do not create panel)
			toolWindow.focusPanelIfExists(projectConfigurationBean.getActiveToolWindowTab());
			toolWindow.getIdeaToolWindow().getContentManager().addContentManagerListener(new ContentManagerAdapter() {
				@Override
				public void selectionChanged(final ContentManagerEvent event) {
					projectConfigurationBean.setActiveToolWindowTab(event.getContent().getDisplayName());
				}
			});

			IdeaHelper.getAppComponent().rescheduleStatusCheckers(false);

			configurationListener = new ConfigurationListenerImpl();
			projectCfgManager.addProjectConfigurationListener(configurationListener);
			projectCfgManager.addProjectConfigurationListener(issuesToolWindowPanel.getConfigListener());

			created = true;

			crucibleEditorFactoryListener = new CrucibleEditorFactoryListener(project,
					crucibleReviewListModel);
			EditorFactory.getInstance()
					.addEditorFactoryListener(crucibleEditorFactoryListener);

			// added here not in plugin.xml - only for Idea 8
			ActionManager aManager = ActionManager.getInstance();
			DefaultActionGroup changesToolBar = (DefaultActionGroup) aManager.getAction("RepositoryChangesBrowserToolbar");
			if (changesToolBar != null) {
				AnAction action = aManager.getAction("ThePlugin.Crucible.ViewFisheyeChangesetItem");
				changesToolBar.remove(action);
				changesToolBar.add(action, aManager);
			}

			registerCrucibleNotifier();
			issuesToolWindowPanel.init();
			checkDefaultServerValues();
		}
	}

	private void registerCrucibleNotifier() {
		crucibleReviewListModel.addListener(crucibleReviewNotifier);
		crucibleTooltip = new CrucibleNotificationTooltip(statusBarCrucibleIcon, project, toolWindow, pluginConfiguration);

		crucibleReviewNotifier.registerListener(crucibleTooltip);
	}


	public void projectOpened() {
		// content moved to StartupManager to wait until
		// here we have guarantee that IDEA splash screen will not obstruct our window
		askForUserStatistics();
		fileEditorListener.projectOpened();

	}

    private void checkDefaultServerValues() {
        String text = "";
        if (projectCfgManager.getDefaultJiraServer() == null && projectCfgManager.getAllJiraServerss().size() > 0) {
            text = "JIRA";
        }


        if (projectCfgManager.getDefaultFishEyeServer() == null && (projectCfgManager.getAllFishEyeServerss().size() > 0
                || projectCfgManager.getAllEnabledCrucibleServersContainingFisheye().size() > 0)) {
            if (text.length() > 0) {
                text += " and FishEye";
            } else {
                text = "FishEye";
            }
        }

        if (text.length() > 0 && !pluginConfiguration.getGeneralConfigurationData().isAskedAboutDefaultServers()) {
            final InformationDialogWithCheckBox dialog = new InformationDialogWithCheckBox(project,
                    PluginUtil.PRODUCT_NAME,
                    "Please set up default " + text + " server in order to get all "
                            + "cool features of " + PluginUtil.PRODUCT_NAME);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dialog.show();
                    pluginConfiguration.getGeneralConfigurationData()
                            .setAskedAboutDefaultServers(dialog.isDoNotShowChecked());
                }
            });
        }


    }

	public FileEditorListenerImpl getFileEditorListener() {
		return fileEditorListener;
	}

	private void askForUserStatistics() {
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			public void run() {
				if (pluginConfiguration.getGeneralConfigurationData().getAnonymousEnhancedFeedbackEnabled() == null) {
					UsageStatsDialog dlg = new UsageStatsDialog();
					dlg.show();
					int answer = dlg.getExitCode();
					pluginConfiguration.getGeneralConfigurationData().setAnonymousEnhancedFeedbackEnabled(
							answer == DialogWrapper.OK_EXIT_CODE);
				}
			}
		}, ModalityState.defaultModalityState());
	}

	private class UsageStatsDialog extends DialogWrapper {
		private static final String MSG_TEXT =
				"We would greatly appreciate it if you would allow us to collect anonymous"
						+ "<br>usage statistics to help us provide a better quality product. Details"
						+ "<br>of what will be tracked are given "
						+ "<a href=\""
						+ UsageStatisticsGenerator.USAGE_STATS_HREF
						+ "\">here</a>. Is this OK?";

		protected UsageStatsDialog() {
			super((Project) null, false);
			init();
			setTitle(PluginUtil.getInstance().getName() + " Request");
			setModal(true);
			setOKButtonText("Yes");
			setCancelButtonText("No");
		}

		@Override
		protected JComponent createCenterPanel() {
			JPanel p = new JPanel(new FormLayout("3dlu, p, 3dlu, p, 3dlu", "3dlu, p, 3dlu"));
			CellConstraints cc = new CellConstraints();

			JEditorPane textPane = new JEditorPane();
			textPane.setContentType("text/html");
			textPane.setEditable(false);
			textPane.setOpaque(false);
			textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
			textPane.setText("<html>" + MSG_TEXT);
			textPane.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						BrowserUtil.launchBrowser(e.getURL().toString());
					}
				}
			});

			p.add(new JLabel(Messages.getQuestionIcon()), cc.xy(2, 2));
			p.add(textPane, cc.xy(4, 2));

			return p;
		}


	}

	public void projectClosed() {
		if (created) {
			fileEditorListener.projectClosed();
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
			//unregister form model
			//crucibleStatusChecker.unregisterListener(crucibleReviewNotifier);
			projectCfgManager.removeProjectConfigurationListener(configurationListener);
			configurationListener = null;
			projectCfgManager.removeProjectConfigurationListener(issuesToolWindowPanel.getConfigListener());

			// remove tool window
			toolWindowManager.unregisterToolWindow(PluginToolWindow.TOOL_WINDOW_NAME);

			EditorFactory.getInstance().removeEditorFactoryListener(crucibleEditorFactoryListener);

			//remove Crucible listeners
			crucibleReviewNotifier.unregisterListener(crucibleTooltip);
			crucibleReviewListModel.removeListener(crucibleReviewNotifier);

			ActionManager aManager = ActionManager.getInstance();
			DefaultActionGroup changesToolBar = (DefaultActionGroup) aManager.getAction("RepositoryChangesBrowserToolbar");
			if (changesToolBar != null) {
				AnAction action = aManager.getAction("ThePlugin.Crucible.ViewFisheyeChangesetItem");
				changesToolBar.remove(action);
			}

			created = false;
		}
	}

	public WorkspaceConfigurationBean getProjectConfigurationBean() {
		return projectConfigurationBean;
	}

	public CrucibleStatusChecker getCrucibleStatusChecker() {
		return crucibleStatusChecker;
	}

	public BambooStatusChecker getBambooStatusChecker() {
		return bambooStatusChecker;
	}

	public JIRAIssueListModelBuilder getJiraIssueListModelBuilder() {
		return jiraIssueListModelBuilder;
	}

	private class ConfigurationListenerImpl extends ConfigurationListenerAdapter {

		@Override
		public void configurationUpdated(final ProjectConfiguration aProjectConfiguration) {
			// show-hide icons if necessary
			statusBarBambooIcon.showOrHideIcon();
			statusBarCrucibleIcon.showOrHideIcon();
			// show-hide panels if necessary
			toolWindow.showHidePanels();
		}
	}

}
