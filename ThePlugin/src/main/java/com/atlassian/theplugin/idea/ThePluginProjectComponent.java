package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooStatusChecker;
import com.atlassian.theplugin.bamboo.BambooStatusListenerImpl;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

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
	private BambooStatusListenerImpl bambooStatusListener;

	public ThePluginProjectComponent(Project project) {
		this.project = project;

		// make findBugs happy
		bambooStatusChecker = null;
		statusBar = null;
		statusBarIcon = null;
	}

	public void initComponent() {
		System.out.println("Init ThePlugin status component.");
	}

	public void disposeComponent() {
		statusBar.setInfo("disposeComponent");
	}

	@NotNull
	public String getComponentName() {
		return "ThePluginProjectComponent";
	}

	public void projectOpened() {
		ThePluginApplicationComponent appComponent =
				ApplicationManager.getApplication().getComponent(ThePluginApplicationComponent.class);

		bambooStatusChecker = appComponent.getBambooStatusChecker();

		statusBarIcon = new BambooStatusIcon(project);
		statusBarIcon.updateBambooStatus(BuildStatus.UNKNOWN, "Waiting for Bamboo build statuses.");

		statusBarComponent = statusBarIcon;

		bambooStatusListener = new BambooStatusListenerImpl(statusBarIcon);
		bambooStatusChecker.registerListener(bambooStatusListener);

		statusBar = WindowManager.getInstance().getStatusBar(project);
		statusBar.addCustomIndicationComponent(statusBarComponent);

		System.out.println("projectOpened");
	}

	public void projectClosed() {

		System.out.println("projectClosed");

		statusBar.removeCustomIndicationComponent(statusBarComponent);
		bambooStatusChecker.unregisterListener(bambooStatusListener);

		statusBarComponent = null;
		statusBarIcon = null;
	}

	public void setBambooStatus(String status, String statusDescription) {
		statusBarIcon.setText(status);
		statusBarIcon.setToolTipText(statusDescription);
	}
}
