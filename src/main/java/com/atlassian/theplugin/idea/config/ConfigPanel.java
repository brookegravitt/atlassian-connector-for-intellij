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

	private HeaderPanel headerPanel = null;
	private FooterPanel footerPanel = null;
	private JTabbedPane contentPanel = null;
	private ServerConfigPanel serverConfigPanel = null;
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

		add(getHeaderPanel(), BorderLayout.NORTH);

		contentPanel = new JTabbedPane();

		// add servers tab
		serverConfigPanel = getServerConfigPanel();
		contentPanel.add(serverConfigPanel.getTitle(), serverConfigPanel);

		// add general tab
		generalConfigPanel = GeneralConfigPanel.getInstance();
		contentPanel.add(generalConfigPanel.getTitle(), generalConfigPanel);

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
			serverConfigPanel = ServerConfigPanel.getInstance();
		}
		return serverConfigPanel;
	}

	public boolean isModified() {
		if (!this.pluginConfiguration.equals(ConfigurationFactory.getConfiguration())) {
            return true;
		}
		return serverConfigPanel.isModified() || headerPanel.isModified();
	}

	public void getData() {
		if (isModified()) {
			headerPanel.getData();
			serverConfigPanel.getData();
			generalConfigPanel.getData();
		}
	}

	public void setData() {
		this.pluginConfiguration = new PluginConfigurationBean(ConfigurationFactory.getConfiguration());
		headerPanel.setData();
		serverConfigPanel.setData();
		generalConfigPanel.setData();
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