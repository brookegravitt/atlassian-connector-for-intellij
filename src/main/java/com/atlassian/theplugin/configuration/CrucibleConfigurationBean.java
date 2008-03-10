package com.atlassian.theplugin.configuration;

public class CrucibleConfigurationBean extends AbstractConfigurationBean {
	private int pollTime = 1;

	public CrucibleConfigurationBean() {
		super();
	}

	public CrucibleConfigurationBean(ProductServerConfiguration cfg) {

		super(cfg);
		if (cfg instanceof CrucibleConfigurationBean) {
			this.pollTime = ((CrucibleConfigurationBean) cfg).getPollTime();
		}
	}

	public int getPollTime() {
		return pollTime;
	}

	public void setPollTime(int pollTime) {
		this.pollTime = pollTime;
	}
}