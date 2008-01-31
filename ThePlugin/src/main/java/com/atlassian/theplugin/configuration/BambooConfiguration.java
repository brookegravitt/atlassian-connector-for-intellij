package com.atlassian.theplugin.configuration;

import java.util.Collection;

/**
 * Bamboo global configuration.
 *
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 11:46:29 AM
 */
public interface BambooConfiguration {
    Collection<Server> getServers();
	void storeServer(Server server);
	void setServers(Collection<Server> servers);
	void removeServer(Server server);
}
