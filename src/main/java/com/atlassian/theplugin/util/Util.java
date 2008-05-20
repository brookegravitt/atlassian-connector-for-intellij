package com.atlassian.theplugin.util;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.exception.ThePluginException;
import com.atlassian.theplugin.idea.PluginToolWindow;

public final class Util {
    private Util() {
    }

    public static ServerType toolWindowPanelsToServerType(PluginToolWindow.ToolWindowPanels panel) throws ThePluginException {
		switch (panel) {
			case BAMBOO:
				return ServerType.BAMBOO_SERVER;
			case CRUCIBLE:
				return ServerType.CRUCIBLE_SERVER;
			case JIRA:
				return ServerType.JIRA_SERVER;
			default:
				throw new ThePluginException("Unrecognized tool window type");
		}
	}
}
