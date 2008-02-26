package com.atlassian.theplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.atlassian.theplugin.configuration.ConfigurationFactory;

import javax.swing.*;

public abstract class StatusBarPluginIcon extends JLabel {
	private StatusBar statusBar = null;

	public StatusBarPluginIcon(Project project) {
		statusBar = WindowManager.getInstance().getStatusBar(project);
	}

	private void hideIcon() {
		statusBar.removeCustomIndicationComponent(this);
	}

	private void showIcon() {
		statusBar.addCustomIndicationComponent(this);
	}

	/**
	 * Shows or hides icon for specified product (Bamboo/Crucible) depending if there are defined servers or not
	 * @param serverType
	 */
	protected void showOrHideIcon(ServerType serverType) {
		if (ConfigurationFactory.getConfiguration().getProductServers(serverType).getServers().size() > 0) {
			showIcon();
		} else {
			hideIcon();
		}
	}
}
