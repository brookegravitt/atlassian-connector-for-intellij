/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.Collection;
import java.util.ArrayList;

public class AbstractConfigurationBean implements ProductServerConfiguration {
    protected Collection<ServerBean> servers = new ArrayList<ServerBean>();

    public AbstractConfigurationBean() {
    }

    public AbstractConfigurationBean(ProductServerConfiguration cfg) {
		for (Server server : cfg.getServers()) {
			Server newServer = new ServerBean(server);
			storeServer(newServer);
		}
	}

    /**
	 * For storage purposes.
     * <p/>
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public Collection<ServerBean> getServersData() {
        return servers;
    }

    /**
	 * For storage purposes.
     * <p/>
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public void setServersData(Collection<ServerBean> theServers) {
        this.servers = theServers;
    }

    /**
	 * Implemnentation for the interface.
     * <p/>
     * Do not mistake for #getServerData()
     *
     * @return
     */
    @Transient
    public synchronized Collection<Server> getServers() {
        ArrayList<Server> iservers = new ArrayList<Server>();
        iservers.addAll(servers);
        return iservers;
    }

    @Transient
	public synchronized Collection<Server> getEnabledServers() {
        ArrayList<Server> iservers = new ArrayList<Server>();
        for (Server s : servers) {
            if (s.getEnabled()) {
                iservers.add(s);
            }
        }
        return iservers;
    }

    @Transient
	public synchronized Server getServer(Server aServer) {
        for (Server server : servers) {
            if (server.getUid() == aServer.getUid()) {
                return server;
            }
        }
        return null;
    }

    @Transient
	public void storeServer(Server server) {
        Server foundServer = getServer(server);
        if (foundServer == null) {
            servers.add((ServerBean) server);
        } else {
            foundServer.setName(server.getName());
            foundServer.setPasswordString(server.getPasswordString(), server.getShouldPasswordBeStored());
            foundServer.setUrlString(server.getUrlString());
            foundServer.setUserName(server.getUserName());
            foundServer.setEnabled(server.getEnabled());
        }
    }

    public void setServers(Collection<Server> servers) {
        this.servers.clear();
        for (Server server : servers) {
            this.servers.add((ServerBean) server);
        }
    }

    @Transient
	public synchronized void removeServer(Server serverToRemove) {
        for (Server server : servers) {
            if (server.getUid() == serverToRemove.getUid()) {
                servers.remove(server);
                break;
            }
        }
    }

    @Transient
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractConfigurationBean that = (AbstractConfigurationBean) o;

        if (!servers.equals(that.servers)) {
            return false;
        }

        return true;
    }

    @Transient
	public int hashCode() {
        return (servers != null ? servers.hashCode() : 0);
    }
}
