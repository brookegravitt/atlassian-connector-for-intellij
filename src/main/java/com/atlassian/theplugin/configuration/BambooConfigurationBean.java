package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.annotations.Transient;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 4:13:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooConfigurationBean implements BambooConfiguration {
	private ServerBean server = new ServerBean();

	/**
	 * For storage purposes.
	 * <p/>
	 * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
	 */
	public ServerBean getServerData() {
		return server;
	}

	/**
	 * For storage purposes.
	 * <p/>
	 * Does not use the JDK1.5 'return a subclass' due to problem with XML serialization.
	 */
	public void setServerData(ServerBean aServer) {
		this.server = aServer;
	}


	/**
	 * Implemnentation for the interface.
	 * <p/>
	 * Do not mistake for #getServerData()
	 *
	 * @return
	 */
	@Transient
	public Server getServer() {
		return server;
	}


}
