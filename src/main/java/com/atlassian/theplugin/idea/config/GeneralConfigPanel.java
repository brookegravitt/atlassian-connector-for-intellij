package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.idea.GeneralConfigForm;

import javax.swing.*;
import java.awt.*;

/**
 * General plugin config form.
 */
public class GeneralConfigPanel extends JPanel implements ContentPanel {
	private GeneralConfigForm dialog;
	private PluginConfiguration localPluginConfigurationCopy;
	private final PluginConfiguration globalPluginConfiguration;

	public GeneralConfigPanel(PluginConfiguration globalPluginConfiguration) {
		super();
		this.globalPluginConfiguration = globalPluginConfiguration;
		setLayout(new CardLayout());
		dialog = new GeneralConfigForm();
		add(dialog.getRootPane(), "GeneralConfig");
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isModified() {
		return !localPluginConfigurationCopy.equals(globalPluginConfiguration)
				|| dialog.getIsAutoUpdateEnabled() != globalPluginConfiguration.isAutoUpdateEnabled();

	}

	public String getTitle() {
		return "General";
	}

	public void getData() {
		localPluginConfigurationCopy.setAutoUpdateEnabled(dialog.getIsAutoUpdateEnabled());
		globalPluginConfiguration.setAutoUpdateEnabled(dialog.getIsAutoUpdateEnabled());
	}

	public void setData(PluginConfiguration config) {
		localPluginConfigurationCopy = config;
		dialog.setAutoUpdateEnabled(localPluginConfigurationCopy.isAutoUpdateEnabled());
	}

}
