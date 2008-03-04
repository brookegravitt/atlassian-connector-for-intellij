package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.idea.config.serverconfig.BambooGeneralForm;
import com.atlassian.theplugin.idea.config.serverconfig.ServerConfigPanel;

import javax.swing.*;
import java.awt.*;

public final class ConfigPanel extends JPanel {
	private static ConfigPanel instance;

	private transient PluginConfigurationBean localPluginConfigurationCopy = null;

	private final FooterPanel footerPanel = new FooterPanel();
	private final JTabbedPane contentPanel = new JTabbedPane();
	private final ServerConfigPanel serverConfigPanel;
	private final BambooGeneralForm bambooConfigPanel;
	private final GeneralConfigPanel generalConfigPanel;

	private final transient PluginConfigurationBean globalConfigurationBean;

	public ConfigPanel(ServerConfigPanel serverConfigPanel,
					   BambooGeneralForm bambooConfigPanel,
					   GeneralConfigPanel generalConfigPanel,
					   PluginConfigurationBean globalConfigurationBean) {
		/* Yes, I mean this. Assigning to a static field from within a constructor. Blame *Action. */ 
		instance = this;
		this.serverConfigPanel = serverConfigPanel;
		this.bambooConfigPanel = bambooConfigPanel;
		this.generalConfigPanel = generalConfigPanel;
		this.globalConfigurationBean = globalConfigurationBean;

		initLayout();
	}

	/**
	 * This one is still here because IDEA complains about AnAction objects having non-parameterless constructor.
	 * @return single instance of ConfigPanel.
	 */
	public static ConfigPanel getInstance() {
		return instance;
	}

	private void initLayout() {
		setLayout(new BorderLayout());

		// add servers tab
		contentPanel.add(serverConfigPanel.getTitle(), serverConfigPanel);

		// add Bamboo optins tab
		contentPanel.add(bambooConfigPanel.getTitle(), bambooConfigPanel);

		// add general tab
		contentPanel.add(generalConfigPanel.getTitle(), generalConfigPanel);

		add(contentPanel, BorderLayout.CENTER);
		add(footerPanel, BorderLayout.SOUTH);

	}



	public boolean isModified() {
		return !this.localPluginConfigurationCopy.equals(globalConfigurationBean)
				|| serverConfigPanel.isModified()
				|| bambooConfigPanel.isModified()
				|| generalConfigPanel.isModified();
	}

	public void getData() {
		if (isModified()) {
			serverConfigPanel.getData();
			generalConfigPanel.getData();
			bambooConfigPanel.getData();
		}
	}

	public void setData() {
		this.localPluginConfigurationCopy = new PluginConfigurationBean(globalConfigurationBean);
		serverConfigPanel.setData(localPluginConfigurationCopy);
		generalConfigPanel.setData(localPluginConfigurationCopy);
		bambooConfigPanel.setData(localPluginConfigurationCopy);
	}

	public void addServer(ServerType serverType) {
		serverConfigPanel.addServer(serverType);
	}

	public void removeServer() {
		serverConfigPanel.removeServer();
	}


	public void copyServer() {
		serverConfigPanel.copyServer();
	}
}