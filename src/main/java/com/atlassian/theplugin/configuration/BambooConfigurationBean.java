package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Bean storing information about Bamboo servers etc.<p>
 * The class serves both as a configuration provider for plugin logic and Bean for persistence.
 */
public class BambooConfigurationBean implements ProductServerConfiguration {
	private Collection<ServerBean> servers = new ArrayList<ServerBean>();

	public BambooConfigurationBean() {
	}

	public BambooConfigurationBean(ProductServerConfiguration cfg) {
		for (Server server : cfg.getServers()) {
			ServerBean newServer = new ServerBean(server);
			servers.add(newServer);
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
	 *
	 * @param theServers Collection of theServers to substitute for the current one.
	 */
	public void setServersData(Collection<ServerBean> theServers) {
		this.servers = theServers;
	}


	/**
	 * Implemnentation for the interface.
	 * <p/>
	 * Do not mistake for #getServerData()
	 *
	 * @return Collection of Servers
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
			foundServer.getSubscribedPlansData().clear();
			for (SubscribedPlan plan : server.getSubscribedPlans()) {
				foundServer.getSubscribedPlansData().add((SubscribedPlanBean) plan);
			}
			foundServer.setUrlString(server.getUrlString());
			foundServer.setUsername(server.getUsername());
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
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		BambooConfigurationBean that = (BambooConfigurationBean) o;

		if (servers != null ? !servers.equals(that.servers) : that.servers != null) {
			return false;
		}

		return true;
	}

	@Transient
	public int hashCode() {
		return (servers != null ? servers.hashCode() : 0);
	}
}
