package com.atlassian.theplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.atlassian.theplugin.configuration.ConfigurationFactory;

import javax.swing.*;

public abstract class StatusBarPluginIcon extends JLabel {
	private StatusBar statusBar = null;
	private Project project;

	public StatusBarPluginIcon(Project aProject) {
		this.project = aProject;
		statusBar = WindowManager.getInstance().getStatusBar(project);
	}

	private void hideIcon() {
		statusBar.removeCustomIndicationComponent(this);
		WindowManager.getInstance().getFrame(project).repaint();
	}

	private void showIcon() {
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
