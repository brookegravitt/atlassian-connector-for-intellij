package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.*;
import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.crucible.HtmlCrucibleStatusListener;
import com.atlassian.theplugin.idea.autoupdate.ConfirmPluginUpdateHandler;
import com.atlassian.theplugin.idea.autoupdate.PluginUpdateIcon;
import com.atlassian.theplugin.idea.bamboo.BambooStatusIcon;
import com.atlassian.theplugin.idea.bamboo.BambooTableToolWindowPanel;
import com.atlassian.theplugin.idea.bamboo.BuildStatusChangedToolTip;
import com.atlassian.theplugin.idea.crucible.*;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.table.TableView;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Per-project plugin component.
 */
@State(name = "ThePluginSettings", storages = { @Storage(id = "thePlugin", file = "$PROJECT_FILE$") })
public class ThePluginProjectComponent implements ProjectComponent, PersistentStateComponent<ProjectConfigurationBean> {
    private static final String THE_PLUGIN_TOOL_WINDOW_ICON = "/icons/thePlugin_15x10.png";

	private final ProjectConfigurationBean projectConfigurationBean;

	private final Project project;
	private BambooStatusIcon statusBarBambooIcon;

	private CrucibleStatusIcon statusBarCrucibleIcon;
	private PluginUpdateIcon statusPluginUpdateIcon;
	private final BambooStatusChecker bambooStatusChecker;
	private HtmlBambooStatusListener iconBambooStatusListener;
	private BambooStatusListenerImpl tooltipBambooStatusListener;

	private final BambooTableToolWindowPanel bambooToolWindowPanel;

	private final CrucibleStatusChecker crucibleStatusChecker;
    private HtmlCrucibleStatusListener toolWindowCrucibleListener;

	private final ToolWindowManager toolWindowManager;
	private boolean created;
	private CrucibleNewReviewNotifier crucibleNewReviewNotifier;

	private final PluginConfiguration pluginConfiguration;

	private CrucibleToolWindowPanel crucibleToolWindowPanel;

	private JIRAToolWindowPanel jiraToolWindowPanel;

	public ThePluginProjectComponent(Project project,
									 CrucibleStatusChecker crucibleStatusChecker,
									 ToolWindowManager toolWindowManager,
									 BambooStatusChecker bambooStatusChecker,
									 PluginConfiguration pluginConfiguration,
									 BambooTableToolWindowPanel bambooToolWindowPanel,
									 ProjectConfigurationBean projectConfigurationBean) {
		this.project = project;
		this.crucibleStatusChecker = crucibleStatusChecker;
		this.toolWindowManager = toolWindowManager;
		this.bambooStatusChecker = bambooStatusChecker;
		this.pluginConfiguration = pluginConfiguration;
		this.bambooToolWindowPanel = bambooToolWindowPanel;
		this.projectConfigurationBean = projectConfigurationBean;
		//this.jiraToolWindowPanel = jiraToolWindowPanel;

		// make findBugs happy
        statusBarBambooIcon = null;
		statusBarCrucibleIcon = null;
		statusPluginUpdateIcon = null;
		created = false;
	}

	public void initComponent() {
		System.out.println("Start: Init ThePlugin project component.");
		System.out.println("End: Init ThePlugin project component.");
		ChangeListManager.getInstance(project).registerCommitExecutor(new CruciblePatchSubmitExecutor(project));
	}

	public void disposeComponent() {
		System.out.println("Start: Dispose ThePlugin project component");
		System.out.println("End: Dispose ThePlugin project component");
	}

	@NotNull
	public String getComponentName() {
		return "ThePluginProjectComponent";
	}

	private void createPlugin() {
        // unregister changelistmanager?
        // only open tool windows for each application that's registered
        // show something nice if there are non
        // swap listener for dataretrievedlistener and datachangelisteners
        // store bamboo between runs in UDC
        // clean up object model confusion

		if (!created) {

			// create tool window on the right
			com.intellij.openapi.wm.ToolWindow toolWindow = toolWindowManager.registerToolWindow(IdeaHelper.TOOL_WINDOW_NAME,
						true, ToolWindowAnchor.RIGHT);
			Icon toolWindowIcon = IconLoader.getIcon(THE_PLUGIN_TOOL_WINDOW_ICON);
			toolWindow.setIcon(toolWindowIcon);

			// create tool window content
			Content bambooToolWindow = createBambooContent();
			toolWindow.getContentManager().addContent(bambooToolWindow);
			TableView.restore(projectConfigurationBean.getBambooConfiguration().getTableConfiguration(),
					bambooToolWindowPanel.getTable());

			crucibleToolWindowPanel = new CrucibleToolWindowPanel();
			Content crucibleToolWindow = createCrusibleContent();
			toolWindow.getContentManager().addContent(crucibleToolWindow);

			jiraToolWindowPanel = new JIRAToolWindowPanel(projectConfigurationBean);
            Content jiraToolWindow = createJiraContent();
			toolWindow.getContentManager().addContent(jiraToolWindow);
            toolWindow.getContentManager().setSelectedContent(jiraToolWindow);
			TableView.restore(projectConfigurationBean.getJiraConfiguration().getTableConfiguration(),
					jiraToolWindowPanel.getTable());

			// add tool window bamboo content listener to bamboo checker thread
			//toolWindowBambooListener = new HtmlBambooStatusListener(bambooToolWindowPanel.getBambooContent());
			bambooStatusChecker.registerListener(bambooToolWindowPanel);

			// create Bamboo status bar icon
			statusBarBambooIcon = new BambooStatusIcon(this.project);
			statusBarBambooIcon.updateBambooStatus(BuildStatus.UNKNOWN,
					"<div style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">"
							+ "Waiting for Bamboo build statuses."
							+ "</div>");

			// add icon listener to bamboo checker thread
			iconBambooStatusListener = new HtmlBambooStatusListener(statusBarBambooIcon);
			bambooStatusChecker.registerListener(iconBambooStatusListener);

			// add simple bamboo listener to bamboo checker thread
			// this listener shows idea tooltip when buld failed
			BambooStatusDisplay buildFailedToolTip = new BuildStatusChangedToolTip(project);
			tooltipBambooStatusListener = new BambooStatusListenerImpl(buildFailedToolTip, pluginConfiguration);
			bambooStatusChecker.registerListener(tooltipBambooStatusListener);

			// add bamboo icon to status bar
			//statusBar = WindowManager.getInstance().getStatusBar(project);
			//statusBar.addCustomIndicationComponent(statusBarBambooIcon);
			statusBarBambooIcon.showOrHideIcon();

			// setup Crucible status checker and listeners
            toolWindowCrucibleListener = new HtmlCrucibleStatusListener(crucibleToolWindowPanel.getCrucibleContent());
            crucibleStatusChecker.registerListener(toolWindowCrucibleListener);

			// create crucible status bar icon
			statusBarCrucibleIcon = new CrucibleStatusIcon(project);

			crucibleNewReviewNotifier = new CrucibleNewReviewNotifier(statusBarCrucibleIcon);
			crucibleStatusChecker.registerListener(crucibleNewReviewNotifier);

			// add crucible icon to status bar
			//statusBar.addCustomIndicationComponent(statusBarCrucibleIcon);
			statusBarCrucibleIcon.showOrHideIcon();

			statusPluginUpdateIcon = new PluginUpdateIcon(project, pluginConfiguration);
			ConfirmPluginUpdateHandler.getInstance().setDisplay(statusPluginUpdateIcon);
			//statusPluginUpdateIcon.showOrHideIcon();


			created = true;
		}
	}

	public Content createBambooContent() {
		PeerFactory peerFactory = PeerFactory.getInstance();

		Content content = peerFactory.getContentFactory().createContent(
				bambooToolWindowPanel,
				IdeaHelper.ToolWindowPanels.BAMBOO.toString(),
				false);

		content.setIcon(IconLoader.getIcon("/icons/bamboo-blue-16.png"));
		content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);

		return content;
	}

	public Content createCrusibleContent() {
		PeerFactory peerFactory = PeerFactory.getInstance();

		Content content = peerFactory.getContentFactory().createContent(
				crucibleToolWindowPanel, IdeaHelper.ToolWindowPanels.CRUCIBLE.toString(), false);
		content.setIcon(IconLoader.getIcon("/icons/crucible-blue-16.png"));
		content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);

		return content;
	}


	public Content createJiraContent() {
		PeerFactory peerFactory = PeerFactory.getInstance();

		Content content = peerFactory.getContentFactory().createContent(
				jiraToolWindowPanel, IdeaHelper.ToolWindowPanels.JIRA.toString(), false);
		content.setIcon(IconLoader.getIcon("/icons/jira-blue-16.png"));
		content.putUserData(com.intellij.openapi.wm.ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);

		return content;
	}

	public void disposePlugin() {
		if (created) {
			// remove icon from status bar
			statusBarBambooIcon.hideIcon();
			statusBarBambooIcon = null;
			statusBarCrucibleIcon.hideIcon();
			statusBarCrucibleIcon = null;
			statusPluginUpdateIcon.hideIcon();
			statusPluginUpdateIcon = null;


			// unregister listeners
			//bambooStatusChecker.unregisterListener(iconBambooStatusListener);
			//bambooStatusChecker.unregisterListener(toolWindowBambooListener);
			bambooStatusChecker.unregisterListener(tooltipBambooStatusListener);
			crucibleStatusChecker.unregisterListener(toolWindowCrucibleListener);
			crucibleStatusChecker.unregisterListener(crucibleNewReviewNotifier);

			// remove tool window
			toolWindowManager.unregisterToolWindow(IdeaHelper.TOOL_WINDOW_NAME);
			created = false;
		}
	}

	public void projectOpened() {

		System.out.println("Start: Project open");
		createPlugin();
		System.out.println("End: Project open");
	}

	public void projectClosed() {
		System.out.println("Start: Project close");
		disposePlugin();
		System.out.println("End: Project close");
	}

	public BambooStatusIcon getStatusBarBambooIcon() {
		return statusBarBambooIcon;
	}

	public CrucibleStatusIcon getStatusBarCrucibleIcon() {
		return statusBarCrucibleIcon;
	}

	public ProjectConfigurationBean getState() {
		System.out.println("GET:");
		return projectConfigurationBean;
	}

	public void loadState(ProjectConfigurationBean state) {
		System.out.println("SET:");
		projectConfigurationBean.copyConfiguration(state);
	}

	public ProjectConfigurationBean getProjectConfigurationBean() {
		return projectConfigurationBean;
	}
}
