package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.*;
import com.atlassian.theplugin.crucible.HtmlCrucibleStatusListener;
import com.atlassian.theplugin.idea.bamboo.BambooStatusIcon;
import com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel;
import com.atlassian.theplugin.idea.bamboo.BuildStatusChangedToolTip;
import com.atlassian.theplugin.idea.crucible.CruciblePatchSubmitExecutor;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusIcon;
import com.atlassian.theplugin.idea.crucible.CrucibleToolWindowPanel;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.wm.*;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Per-project plugin component.
 */
public class ThePluginProjectComponent implements ProjectComponent {
    private static final String THE_PLUGIN_TOOL_WINDOW_ICON = "/icons/thePlugin_15x10.png";

	private final Project project;
	private BambooStatusIcon statusBarBambooIcon;

	private CrucibleStatusIcon statusBarCrucibleIcon;
	private BambooStatusChecker bambooStatusChecker;
	private HtmlBambooStatusListener iconBambooStatusListener;
    private HtmlBambooStatusListener toolWindowBambooListener;
	private BambooStatusListenerImpl tooltipBambooStatusListener;

	private CrucibleStatusChecker crucibleStatusChecker;
    private HtmlCrucibleStatusListener toolWindowCrucibleListener;

	private ToolWindowManager toolWindowManager;
	private boolean enabled;
	private BambooStatusDisplay buildFailedToolTip;
	private ToolWindow toolWindow;
	private UserDataContext crucibleUserContext;

	private ThePluginApplicationComponent applicationComponent;

	public ThePluginProjectComponent(Project project, ThePluginApplicationComponent applicationComponent) {
		this.project = project;
		this.applicationComponent = applicationComponent;
		applicationComponent.setProjectComponent(this);

		// make findBugs happy
		toolWindowManager = null;
		bambooStatusChecker = null;
        crucibleStatusChecker = null;
		statusBarBambooIcon = null;
		statusBarCrucibleIcon = null;
		enabled = false;
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

	public void enablePlugin() {
        // unregister changelistmanager?
        // only open tool windows for each application that's registered
        // show something nice if there are non
        // swap listener for dataretrievedlistener and datachangelisteners
        // store bamboo between runs in UDC
        // clean up object model confusion

		bambooStatusChecker = applicationComponent.getBambooStatusChecker();

		if (!enabled) {

			// create tool window on the right
			toolWindowManager = ToolWindowManager.getInstance(project);
			toolWindow = toolWindowManager.registerToolWindow(IdeaHelper.TOOL_WINDOW_NAME, true, ToolWindowAnchor.RIGHT);
			Icon toolWindowIcon = IconLoader.getIcon(THE_PLUGIN_TOOL_WINDOW_ICON);
			toolWindow.setIcon(toolWindowIcon);

			// create tool window content
			BambooToolWindowPanel bambooToolWindowPanel = new BambooToolWindowPanel();
			PeerFactory peerFactory = PeerFactory.getInstance();

			Content bambooToolWindow = peerFactory.getContentFactory().createContent(
					bambooToolWindowPanel, IdeaHelper.TOOLWINDOW_PANEL_BAMBOO, false);
			bambooToolWindow.setIcon(IconLoader.getIcon("/icons/bamboo-blue-16.png"));
			bambooToolWindow.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
			toolWindow.getContentManager().addContent(bambooToolWindow);

			CrucibleToolWindowPanel crucibleToolWindowPanel = new CrucibleToolWindowPanel();
			Content crucibleToolWindow = peerFactory.getContentFactory().createContent(
					crucibleToolWindowPanel, IdeaHelper.TOOLWINDOW_PANEL_CRUCIBLE, false);
			crucibleToolWindow.setIcon(IconLoader.getIcon("/icons/crucible-blue-16.png"));
			crucibleToolWindow.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
			toolWindow.getContentManager().addContent(crucibleToolWindow);

            JIRAToolWindowPanel jiraToolWindowPanel = new JIRAToolWindowPanel();
            Content jiraToolWindow = peerFactory.getContentFactory().createContent(
                    jiraToolWindowPanel, IdeaHelper.TOOLWINDOW_PANEL_JIRA, false);
            jiraToolWindow.setIcon(IconLoader.getIcon("/icons/jira-blue-16.png"));
			jiraToolWindow.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
			toolWindow.getContentManager().addContent(jiraToolWindow);
            toolWindow.getContentManager().setSelectedContent(jiraToolWindow);

            // add tool window bamboo content listener to bamboo checker thread
			toolWindowBambooListener = new HtmlBambooStatusListener(bambooToolWindowPanel.getBambooContent());
			bambooStatusChecker.registerListener(toolWindowBambooListener);

			// create Bamboo status bar icon
			statusBarBambooIcon = new BambooStatusIcon(this);
			statusBarBambooIcon.updateBambooStatus(BuildStatus.UNKNOWN,
					"<div style=\"font-size:12pt ; font-family: arial, helvetica, sans-serif\">"
							+ "Waiting for Bamboo build statuses."
							+ "</div>");

			// add icon listener to bamboo checker thread
			iconBambooStatusListener = new HtmlBambooStatusListener(statusBarBambooIcon);
			bambooStatusChecker.registerListener(iconBambooStatusListener);

			// add simple bamboo listener to bamboo checker thread
			// this listener shows idea tooltip when buld failed
			buildFailedToolTip = new BuildStatusChangedToolTip(project);
			tooltipBambooStatusListener = new BambooStatusListenerImpl(buildFailedToolTip);
			bambooStatusChecker.registerListener(tooltipBambooStatusListener);

			// add bamboo icon to status bar
			//statusBar = WindowManager.getInstance().getStatusBar(project);
			//statusBar.addCustomIndicationComponent(statusBarBambooIcon);
			statusBarBambooIcon.showOrHideIcon();

			// create crucible status bar icon
			statusBarCrucibleIcon = new CrucibleStatusIcon(project);

			// setup Crucible status checker and listeners
            crucibleStatusChecker = CrucibleStatusChecker.getIntance();
            toolWindowCrucibleListener = new HtmlCrucibleStatusListener(crucibleToolWindowPanel.getCrucibleContent());
            crucibleStatusChecker.registerListener(toolWindowCrucibleListener);
			crucibleUserContext = applicationComponent.getUserDataContext();
			crucibleUserContext.setDisplay(statusBarCrucibleIcon);
			crucibleStatusChecker.registerListener(crucibleUserContext);

			// add crucible icon to status bar
			//statusBar.addCustomIndicationComponent(statusBarCrucibleIcon);
			statusBarCrucibleIcon.showOrHideIcon();

			enabled = true;
		}
	}

	public void disablePlugin() {
		if (enabled) {
			// remove icon from status bar
			statusBarBambooIcon.showOrHideIcon();
			statusBarBambooIcon = null;
			statusBarCrucibleIcon.showOrHideIcon();
			statusBarCrucibleIcon = null;


			// unregister listeners
			bambooStatusChecker.unregisterListener(iconBambooStatusListener);
			bambooStatusChecker.unregisterListener(toolWindowBambooListener);
			bambooStatusChecker.unregisterListener(tooltipBambooStatusListener);
			crucibleStatusChecker.unregisterListener(toolWindowCrucibleListener);
			crucibleStatusChecker.unregisterListener(crucibleUserContext);

			// remove tool window
			toolWindowManager.unregisterToolWindow(IdeaHelper.TOOL_WINDOW_NAME);
			toolWindow = null;
			enabled = false;
		}
	}

	public void projectOpened() {

		System.out.println("Start: Project open");
		enablePlugin();
		System.out.println("End: Project open");
	}

	public void projectClosed() {
		System.out.println("Start: Project close");
		disablePlugin();
		System.out.println("End: Project close");
	}

	public Project getProject() {
		return project;
	}

	public ToolWindow getToolWindow() {
		return toolWindow;
	}

	public BambooStatusIcon getStatusBarBambooIcon() {
		return statusBarBambooIcon;
	}

	public CrucibleStatusIcon getStatusBarCrucibleIcon() {
		return statusBarCrucibleIcon;
	}
}
