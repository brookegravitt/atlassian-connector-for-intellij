package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.configuration.ServerBean;

import javax.swing.*;

public interface ServerPanel {
	JComponent getRootComponent();

	boolean isModified();

	ServerBean getData();

	void setData(Server server);
}
