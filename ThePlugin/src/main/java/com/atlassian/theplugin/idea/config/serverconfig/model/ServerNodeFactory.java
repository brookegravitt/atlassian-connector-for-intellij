package com.atlassian.theplugin.idea.config.serverconfig.model;

import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.ServerType;

public final class ServerNodeFactory {
    private ServerNodeFactory() { }

    public static ServerNode getServerNode(ServerType serverType, Server server) {
        switch (serverType) {
            case BAMBOO_SERVER:
                return new BambooServerNode((ServerBean) server);
            case JIRA_SERVER:
                return new JIRAServerNode((ServerBean) server);
            case CRUCIBLE_SERVER:
                return new CrucibleServerNode((ServerBean) server);
            default:
                return null;
        }
    }
}
