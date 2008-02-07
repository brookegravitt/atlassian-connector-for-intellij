package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.configuration.ConfigurationFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-02-05
 * Time: 11:42:01
 * To change this template use File | Settings | File Templates.
 */
public class HeaderPanel extends AbstractContentPanel {
	private JCheckBox enablePlugin;
	private boolean enabled;

	public HeaderPanel() {
		initLayout();
    }

	private void initLayout() {
        BorderLayout gb = new BorderLayout();
		setLayout(gb);
		enablePlugin = new JCheckBox("Enable plugin");
		enablePlugin.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				enabled = ((JCheckBox) e.getSource()).isSelected();
			}
		});
		add(enablePlugin, BorderLayout.WEST);
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isModified() {
		return ConfigPanel.getInstance().getPluginConfiguration().isPluginEnabled() != enabled;
	}

	public String getTitle() {
		return null;
	}

	public void getData() {
		if (isModified()) {
			ConfigurationFactory.getConfiguration().setPluginEnabled(enabled);
		}
	}

	public void setData() {
		enabled = ConfigPanel.getInstance().getPluginConfiguration().isPluginEnabled();
		enablePlugin.setSelected(enabled);
	}
}