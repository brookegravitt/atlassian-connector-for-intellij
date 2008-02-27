package com.atlassian.theplugin.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.ServerType;

import javax.swing.*;

public abstract class StatusBarPluginIcon extends JLabel {
	private StatusBar statusBar = null;
	private Project project;

	public StatusBarPluginIcon(Project aProject) {
		this.project = aProject;
		statusBar = WindowManager.getInstance().getStatusBar(project);
	}

	public void hideIcon() {
		statusBar.removeCustomIndicationComponent(this);
		WindowManager.getInstance().getFrame(project).repaint();
	}

	public void showIcon() {
		statusBar.addCustomIndicationComponent(this);
		WindowManager.getInstance().getFrame(project).repaint();
	}

	/**
	 * Shows or hides icon for specified product (Bamboo/Crucible) depending if there are defined servers or not
	 * @param serverType type of Icon to show/hide
	 */
	protected void showOrHideIcon(ServerType serverType) {
		if (ConfigurationFactory.getConfiguration().getProductServers(serverType).getServers().size() > 0) {
			showIcon();
		} else {
			hideIcon();
		}
	}
}
