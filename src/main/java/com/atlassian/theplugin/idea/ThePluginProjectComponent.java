package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.bamboo.HtmlBambooStatusListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.*;
import com.intellij.peer.PeerFactory;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 14, 2008
 * Time: 3:41:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThePluginProjectComponent implements ProjectComponent {
	private final Project project;
	private StatusBar statusBar;
	private JComponent statusBarComponent;
	private BambooStatusIcon statusBarIcon;
	private BambooStatusChecker bambooStatusChecker;
	private HtmlBambooStatusListener iconBambooStatusListener;

	public ThePluginProjectComponent(Project project) {
		this.project = project;

		// make findBugs happy
		bambooStatusChecker = null;
		statusBar = null;
		statusBarIcon = null;
	}

	public void initComponent() {
		System.out.println("Start: Init ThePlugin project component.");
		System.out.println("End: Init ThePlugin project component.");
	}

	public void disposeComponent() {
		System.out.println("Start: Dispose ThePlugin project component");
		System.out.println("End: Dispose ThePlugin project component");
	}

	@NotNull
	public String getComponentName() {
		return "ThePluginProjectComponent";
	}

	public void projectOpened() {
		System.out.println("Start: Project open");

		ThePluginApplicationComponent appComponent =
				ApplicationManager.getApplication().getComponent(ThePluginApplicationComponent.class);

		// create status bar icon
		statusBarIcon = new BambooStatusIcon(project);
		statusBarIcon.updateBambooStatus(BuildStatus.UNKNOWN, "Waiting for Bamboo build statuses.");
		statusBarComponent = statusBarIcon;

		// add listener to bamboo checker thread
		iconBambooStatusListener = new HtmlBambooStatusListener(statusBarIcon);
		bambooStatusChecker = appComponent.getBambooStatusChecker();
		bambooStatusChecker.registerListener(iconBambooStatusListener);

		// add icon to status bar
		statusBar = WindowManager.getInstance().getStatusBar(project);
		statusBar.addCustomIndicationComponent(statusBarComponent);

		// create tool window on the right
		ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
		ToolWindow toolWindow = toolWindowManager.registerToolWindow("ThePlugin", true, ToolWindowAnchor.RIGHT);
		Icon toolWindowIcon = IconLoader.getIcon("/icons/thePlugin_15x10.png");
		toolWindow.setIcon(toolWindowIcon);

		JPanel toolWindowPanel = new JPanel(new BorderLayout());
		ToolWindowContent xxx = new ToolWindowContent();
		toolWindowPanel.add(xxx, BorderLayout.NORTH);
		xxx.setText("example content");

		PeerFactory peerFactory = PeerFactory.getInstance();
		Content toolWindowContent = peerFactory.getContentFactory().createContent(toolWindowPanel, "", false);

		toolWindow.getContentManager().addContent(toolWindowContent);

		HtmlBambooStatusListener toolWindowStatusListener = new HtmlBambooStatusListener(xxx);
		bambooStatusChecker.registerListener(toolWindowStatusListener);
		

		System.out.println("End: Project open");
	}

	public void projectClosed() {

		System.out.println("Start: Project close");

		statusBar.removeCustomIndicationComponent(statusBarComponent);
		bambooStatusChecker.unregisterListener(iconBambooStatusListener);

		statusBarComponent = null;
		statusBarIcon = null;

		System.out.println("End: Project close");
	}

	public void setBambooStatus(String status, String statusDescription) {
		statusBarIcon.setText(status);
		statusBarIcon.setToolTipText(statusDescription);
	}
}
