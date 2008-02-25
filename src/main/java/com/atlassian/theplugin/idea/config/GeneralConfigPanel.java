package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.idea.GeneralConfigDialog;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-22
 * Time: 13:53:32
 * To change this template use File | Settings | File Templates.
 */
public class GeneralConfigPanel extends AbstractContentPanel {
	private static GeneralConfigPanel instance = null;
	private boolean isPluginEnabled;
	private GeneralConfigDialog dialog;

	public GeneralConfigPanel() {
		super();
		dialog = new GeneralConfigDialog();
		dialog.pack();
		JPanel generalSettingsPanel = dialog.getGeneralSettingsPanel();
		add(generalSettingsPanel);
		generalSettingsPanel.setBounds(this.getBounds());
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isModified() {
        if (!getPluginConfiguration().equals(ConfigurationFactory.getConfiguration())) {
            return true;
        }
        return false;
    }

	public String getTitle() {
		return "General";
	}

	public void getData() {
		ConfigurationFactory.getConfiguration().setAutoUpdateEnabled(dialog.getIsAutoUpdateEnabled());
	}

	public void setData() {
		dialog.setAutoUpdateEnabled(ConfigurationFactory.getConfiguration().isAutoUpdateEnabled());
	}

	public static GeneralConfigPanel getInstance() {
		if (instance == null) {
			instance = new GeneralConfigPanel();
		}

		return instance;
	}

	public PluginConfiguration getPluginConfiguration() {
        return ConfigPanel.getInstance().getPluginConfiguration();
    }
}
