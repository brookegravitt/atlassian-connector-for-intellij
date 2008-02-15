package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.Collection;
import java.util.ArrayList;

public class AbstractConfigurationBean implements ProductServerConfiguration
{
    protected Collection<ServerBean> servers = new ArrayList<ServerBean>();

    public AbstractConfigurationBean()
    {
    }

    public AbstractConfigurationBean(ProductServerConfiguration cfg) {
		for (Server server : cfg.getServers()) {
			ServerBean newServer = new ServerBean(server);
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
        ServerBean foundServer = (ServerBean) getServer(server);
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
	public synchronized void removeServer(Server server) {
        for (ServerBean serverBean : servers) {
            if (serverBean.getUid() == server.getUid()) {
                servers.remove(serverBean);
                break;
            }
        }
    }

    @Transient
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractConfigurationBean that = (AbstractConfigurationBean) o;

        if (!servers.equals(that.servers)) return false;

        return true;
    }

    @Transient
	public int hashCode() {
        return (servers != null ? servers.hashCode() : 0);
    }
}
