package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.configuration.ConfigurationFactory;
import com.atlassian.theplugin.configuration.PluginConfiguration;
import com.atlassian.theplugin.idea.GeneralConfigForm;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-22
 * Time: 13:53:32
 * To change this template use File | Settings | File Templates.
 */
public class GeneralConfigPanel extends JPanel implements ContentPanel {
	private static GeneralConfigPanel instance = null;
	private boolean isPluginEnabled;
	private GeneralConfigForm dialog;
	private PluginConfiguration localPluginConfigurationCopy;

	public GeneralConfigPanel() {
		super();
		setLayout(new CardLayout());
		dialog = new GeneralConfigForm();
		add(dialog.getRootPane(), "GeneralConfig");
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isModified() {
        if (!getPluginConfiguration().equals(ConfigurationFactory.getConfiguration())) {
            return true;
        }

		if (dialog.getIsAutoUpdateEnabled() != ConfigurationFactory.getConfiguration().isAutoUpdateEnabled()) {
			return true;
		}
		return false;
    }

	public String getTitle() {
		return "General";
	}

	public void getData() {
		getPluginConfiguration().setAutoUpdateEnabled(dialog.getIsAutoUpdateEnabled());
		ConfigurationFactory.getConfiguration().setAutoUpdateEnabled(dialog.getIsAutoUpdateEnabled());
	}

	public void setData(PluginConfiguration config) {
		localPluginConfigurationCopy = config;
		dialog.setAutoUpdateEnabled(localPluginConfigurationCopy.isAutoUpdateEnabled());
	}

	public static GeneralConfigPanel getInstance() {
		if (instance == null) {
			instance = new GeneralConfigPanel();
		}

		return instance;
	}

	public PluginConfiguration getPluginConfiguration() {
        return localPluginConfigurationCopy;
    }
}
