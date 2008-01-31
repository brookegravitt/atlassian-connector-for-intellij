package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 4:13:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooConfigurationBean implements BambooConfiguration, Cloneable {
	private Collection<ServerBean> servers = new ArrayList<ServerBean>();

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
	public void setServersData(Collection<ServerBean> servers) {
		this.servers = servers;
	}


	/**
	 * Implemnentation for the interface.
	 * <p/>
	 * Do not mistake for #getServerData()
	 *
	 * @return
	 */
	@Transient
	public Collection<Server> getServers() {
		ArrayList<Server> iservers = new ArrayList<Server>();
		iservers.addAll(servers);
		return iservers;
	}

	@Transient
	public Server getServer(Server aServer) {
		for (Server server : servers) {
			if (server.equals(aServer)) {
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
			try {
				foundServer.setPasswordString(server.getPasswordString(), ((ServerBean) server).getShouldPasswordBeStored());
			} catch (ServerPasswordNotProvidedException e) {
				e.printStackTrace();
			}
			foundServer.getSubscribedPlansData().clear();
			for (SubscribedPlan plan : server.getSubscribedPlans()) {
				foundServer.getSubscribedPlansData().add((SubscribedPlanBean) plan);
			}
			foundServer.setUrlString(server.getUrlString());
			foundServer.setUsername(server.getUsername());
		}
	}

	public void setServers(Collection<Server> servers) {
		this.servers.clear();
		for (Server server : servers) {
			this.servers.add((ServerBean) server);
		}

	}

	public void removeServer(Server server) {
		for (ServerBean serverBean : servers) {
			if (serverBean.equals((ServerBean) server)) {
				servers.remove(serverBean);
			}
		}
	}

	public Object clone() throws CloneNotSupportedException {
		BambooConfigurationBean bambooBean = new BambooConfigurationBean();

		List<ServerBean> servers = new ArrayList<ServerBean>();
		for (ServerBean server : this.getServersData()) {
			ServerBean newServer = (ServerBean) server.clone();
			servers.add(newServer);
		}
		bambooBean.setServersData(servers);

		return bambooBean;

	}
}
