package com.atlassian.theplugin.configuration;

import java.util.Collection;

/**
 * Bamboo global configuration.
 *
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 11:46:29 AM
 */
public interface ProductServerConfiguration {
    Collection<Server> getServers();
	Collection<Server> getEnabledServers();
	Server getServer(Server aServer);
    void storeServer(Server server);
	void setServers(Collection<Server> servers);
	void removeServer(Server server);
}
