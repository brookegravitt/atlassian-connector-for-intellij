package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.configuration.*;
import com.atlassian.theplugin.idea.config.serverconfig.*;
import com.atlassian.theplugin.idea.config.serverconfig.model.ServerNode;
import com.atlassian.theplugin.ServerType;

import javax.swing.*;
import java.awt.*;

public final class ConfigPanel extends JPanel {
	private static ConfigPanel instance;

	private transient PluginConfigurationBean pluginConfiguration = null;

	private FooterPanel footerPanel = null;
	private JTabbedPane contentPanel = null;
	private ServerConfigPanel serverConfigPanel = null;
	private BambooGeneralForm bambooConfigPanel = null;
	private GeneralConfigPanel generalConfigPanel = null;

	private ConfigPanel() {
		initLayout();
	}

	public static ConfigPanel getInstance() {
		if (instance == null) {
			instance = new ConfigPanel();
		}
		return instance;
	}

	private void initLayout() {
		setLayout(new BorderLayout());

		contentPanel = new JTabbedPane();

		// add servers tab
		serverConfigPanel = getServerConfigPanel();
		contentPanel.add(serverConfigPanel.getTitle(), serverConfigPanel);

		// add Bamboo optins tab
		bambooConfigPanel = getBambooConfigPanel();
		contentPanel.add(bambooConfigPanel.getTitle(), bambooConfigPanel);

		// add general tab
		generalConfigPanel = GeneralConfigPanel.getInstance();
		contentPanel.add(generalConfigPanel.getTitle(), generalConfigPanel);

		add(contentPanel, BorderLayout.CENTER);
		add(getFooterPanel(), BorderLayout.SOUTH);

	}

	private JPanel getFooterPanel() {
		if (footerPanel == null) {
			footerPanel = new FooterPanel();
		}
		return footerPanel;
	}

	public ServerConfigPanel getServerConfigPanel() {
		if (serverConfigPanel == null) {
			serverConfigPanel = ServerConfigPanel.getInstance();
		}
		return serverConfigPanel;
	}

	public BambooGeneralForm getBambooConfigPanel() {
		if (bambooConfigPanel == null) {
			bambooConfigPanel = new BambooGeneralForm();
		}
		return bambooConfigPanel;
	}

	public boolean isModified() {
		if (!this.pluginConfiguration.equals(ConfigurationFactory.getConfiguration())) {
            return true;
		}
		return serverConfigPanel.isModified() || bambooConfigPanel.isModified() || generalConfigPanel.isModified();
	}

	public void getData() {
		if (isModified()) {
			serverConfigPanel.getData();
			generalConfigPanel.getData();
			bambooConfigPanel.getData();
		}
	}

	public void setData() {
		this.pluginConfiguration = new PluginConfigurationBean(ConfigurationFactory.getConfiguration());
		serverConfigPanel.setData();
		generalConfigPanel.setData();
		bambooConfigPanel.setData();
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

    public void storeServer(ServerNode serverNode) {
        serverConfigPanel.storeServer(serverNode);
    }

    public PluginConfigurationBean getPluginConfiguration() {
		return pluginConfiguration;
	}

	public void setPluginConfiguration(PluginConfigurationBean pluginConfiguration) {
		this.pluginConfiguration = pluginConfiguration;
	}
}