package com.atlassian.theplugin.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-03-17
 * Time: 16:33:11
 * To change this template use File | Settings | File Templates.
 */
public class BambooProjectConfiguration {
	private BambooProjectToolWindowTableConfiguration tableConfiguration =
			new BambooProjectToolWindowTableConfiguration();

	public BambooProjectConfiguration() {

	}

	public BambooProjectToolWindowTableConfiguration getTableConfiguration() {
		return tableConfiguration;
	}

	public void setTableConfiguration(BambooProjectToolWindowTableConfiguration tableConfiguration) {
		this.tableConfiguration = tableConfiguration;
	}
}
