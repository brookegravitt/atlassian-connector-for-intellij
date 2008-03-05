package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.configuration.Server;

import javax.swing.*;

public interface ServerPanel {
	JComponent getRootComponent();

	boolean isModified();

	Server getData();

	void setData(Server server);
}
