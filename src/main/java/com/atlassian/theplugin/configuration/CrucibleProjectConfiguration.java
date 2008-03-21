package com.atlassian.theplugin.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-03-17
 * Time: 16:33:11
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleProjectConfiguration {
	private ProjectToolWindowTableConfiguration tableConfiguration =
			new ProjectToolWindowTableConfiguration();

	public CrucibleProjectConfiguration() {

	}

	public ProjectToolWindowTableConfiguration getTableConfiguration() {
		return tableConfiguration;
	}

	public void setTableConfiguration(ProjectToolWindowTableConfiguration tableConfiguration) {
		this.tableConfiguration = tableConfiguration;
	}

	public void copyConfiguration(CrucibleProjectConfiguration crucibleConfiguration) {
		tableConfiguration.copyConfiguration(crucibleConfiguration.getTableConfiguration());
	}
}