package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.ArrayList;
import java.util.Collection;

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
     *
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public Collection<ServerBean> getServersData() {
       return servers;
    }

    /**
     * For storage purposes.
     *
     * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
     */
    public void setServersData(Collection<ServerBean> servers) {
        this.servers = servers;
    }


    /**
     * Implemnentation for the interface.
     *
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
	public void addServer(Server server) {
		servers.add((ServerBean)server);
	}

	public void setServers(Collection<Server> servers) {
		this.servers.clear();
	 	for(Server server: servers) {
			 this.servers.add((ServerBean)server);
		 }

	}

	protected Object clone() throws CloneNotSupportedException {
		BambooConfigurationBean bambooBean = new BambooConfigurationBean();

		bambooBean.setServers(this.getServers());

		return bambooBean;

	}
}
