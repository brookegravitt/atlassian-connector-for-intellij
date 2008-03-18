package com.atlassian.theplugin.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-03-17
 * Time: 10:28:55
 * To change this template use File | Settings | File Templates.
 */
public class ProjectConfigurationBean {

	private BambooProjectConfiguration bambooConfiguration = new BambooProjectConfiguration();

	public BambooProjectConfiguration getBambooConfiguration() {
		return bambooConfiguration;
	}

	public void setBambooConfiguration(BambooProjectConfiguration bambooConfiguration) {
		this.bambooConfiguration = bambooConfiguration;
	}

	public ProjectConfigurationBean() {

	}

}
