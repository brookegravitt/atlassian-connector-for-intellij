package com.atlassian.theplugin.idea.config.serverconfig.model;

import com.atlassian.theplugin.configuration.ServerBean;
import com.atlassian.theplugin.configuration.Server;
import com.atlassian.theplugin.ServerType;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-02-06
 * Time: 20:02:47
 * To change this template use File | Settings | File Templates.
 */
public class ServerNodeFactory {
    public static ServerNode getServerNode(ServerType serverType, Server server) {
        switch (serverType) {
            case BAMBOO_SERVER:
                return new BambooServerNode((ServerBean)server);
            case CRUCIBLE_SERVER:
                return new CrucibleServerNode((ServerBean)server);
        }
        return null;
    }
}
