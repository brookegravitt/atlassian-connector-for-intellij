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

	private boolean isIconShown = false;

	public StatusBarPluginIcon(Project aProject) {
		this.project = aProject;
		statusBar = WindowManager.getInstance().getStatusBar(project);
	}

	public void hideIcon() {
		if (statusBar == null) {
			return;
		}
		if (isIconShown) {
			statusBar.removeCustomIndicationComponent(this);
			WindowManager.getInstance().getFrame(project).repaint();
			isIconShown = false;
		}
	}

	public void showIcon() {
		if (statusBar == null) {
			return;
		}
		if (!isIconShown) {
			statusBar.addCustomIndicationComponent(this);
			WindowManager.getInstance().getFrame(project).repaint();
			isIconShown = true;
		}
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

	public boolean isIconShown() {
		return isIconShown;
	}
}
