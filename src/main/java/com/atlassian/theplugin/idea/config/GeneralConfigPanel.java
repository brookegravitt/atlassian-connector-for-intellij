package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.idea.GeneralConfigForm;
import com.atlassian.theplugin.idea.autoupdate.NewVersionChecker;

import javax.swing.*;
import java.awt.*;

/**
 * General plugin config form.
 */
public class GeneralConfigPanel extends JPanel implements ContentPanel {
	private GeneralConfigForm dialog;
	private PluginConfiguration localPluginConfigurationCopy;
	private final PluginConfiguration globalPluginConfiguration;

	public GeneralConfigPanel(PluginConfiguration globalPluginConfiguration, NewVersionChecker checker) {
		super();
		this.globalPluginConfiguration = globalPluginConfiguration;
		localPluginConfigurationCopy = this.globalPluginConfiguration;
		setLayout(new CardLayout());
		dialog = new GeneralConfigForm(checker, localPluginConfigurationCopy);
		add(dialog.getRootPane(), "GeneralConfig");
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isModified() {
		return !localPluginConfigurationCopy.equals(globalPluginConfiguration)
				|| dialog.getIsAutoUpdateEnabled() != globalPluginConfiguration.isAutoUpdateEnabled()
				|| dialog.getIsCheckUnstableVersionsEnabled() != globalPluginConfiguration.getCheckUnstableVersionsEnabled()
				|| dialog.getIsAnonymousFeedbackEnabled() != globalPluginConfiguration.getIsAnonymousFeedbackEnabled();

	}

	public String getTitle() {
		return "General";
	}

	public void getData() {
		localPluginConfigurationCopy.setAutoUpdateEnabled(dialog.getIsAutoUpdateEnabled());
		localPluginConfigurationCopy.setCheckUnstableVersionsEnabled(dialog.getIsCheckUnstableVersionsEnabled());
		localPluginConfigurationCopy.setIsAnonymousFeedbackEnabled(dialog.getIsAnonymousFeedbackEnabled());
		globalPluginConfiguration.setAutoUpdateEnabled(dialog.getIsAutoUpdateEnabled());
		globalPluginConfiguration.setCheckUnstableVersionsEnabled(dialog.getIsCheckUnstableVersionsEnabled());
		globalPluginConfiguration.setIsAnonymousFeedbackEnabled(dialog.getIsAnonymousFeedbackEnabled());
	}

	public void setData(PluginConfiguration config) {
		localPluginConfigurationCopy = config;
		dialog.setAutoUpdateEnabled(localPluginConfigurationCopy.isAutoUpdateEnabled());
		dialog.setIsCheckUnstableVersionsEnabled(localPluginConfigurationCopy.getCheckUnstableVersionsEnabled());
		dialog.setIsAnonymousFeedbackEnabled(localPluginConfigurationCopy.getIsAnonymousFeedbackEnabled());
	}

}
