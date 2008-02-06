package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.configuration.*;
import com.atlassian.theplugin.idea.config.serverconfig.*;

import javax.swing.*;
import java.awt.*;

public class ConfigPanel extends JPanel {
	private static ConfigPanel instance;

	private PluginConfiguration pluginConfiguration = null;

	private HeaderPanel headerPanel = null;
	private FooterPanel footerPanel = null;
	private JTabbedPane contentPanel = null;
	private ServerConfigPanel serverConfigPanel = null;

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

		add(getHeaderPanel(), BorderLayout.NORTH);

		contentPanel = new JTabbedPane();
		serverConfigPanel = getServerConfigPanel();
		contentPanel.add(serverConfigPanel.getTitle(), serverConfigPanel);
		add(contentPanel, BorderLayout.CENTER);

		add(getFooterPanel(), BorderLayout.SOUTH);

	}

	private JPanel getHeaderPanel() {
		if (headerPanel == null) {
			headerPanel = new HeaderPanel();
		}
		return headerPanel;
	}

	private JPanel getFooterPanel() {
		if (footerPanel == null) {
			footerPanel = new FooterPanel();
		}
		return footerPanel;
	}

	public ServerConfigPanel getServerConfigPanel() {
		if (serverConfigPanel == null) {
			serverConfigPanel = new ServerConfigPanel();
		}
		return serverConfigPanel;
	}

	public boolean isModified() {
		if (!this.pluginConfiguration.equals(ConfigurationFactory.getConfiguration())) {
			return true;
		}
		return serverConfigPanel.isModified();
	}

	public void getData() {
		if (isModified()) {
			serverConfigPanel.getData();
		}
	}

	public void setData() {
		clonePluginConfiguration();
		serverConfigPanel.setData(pluginConfiguration);
	}

	public void addBambooServer() {
		serverConfigPanel.addBambooServer();
	}

	public void addCrucibleServer() {
		serverConfigPanel.addCrucibleServer();
	}

	public void removeServer() {
		serverConfigPanel.removeServer();
	}


	public void copyServer() {
		serverConfigPanel.copyServer();
	}

	public void storeBambooServer(ServerBean server) {
		serverConfigPanel.storeBambooServer(server);
	}

	public PluginConfiguration getPluginConfiguration() {
		return pluginConfiguration;
	}

	public void setPluginConfiguration(PluginConfiguration pluginConfiguration) {
		this.pluginConfiguration = pluginConfiguration;
	}

	synchronized private void clonePluginConfiguration() {
		try {
			this.pluginConfiguration = (PluginConfiguration) ((PluginConfigurationBean) ConfigurationFactory.getConfiguration()).clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

}