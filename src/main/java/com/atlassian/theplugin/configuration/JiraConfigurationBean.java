package com.atlassian.theplugin.configuration;

/**
 * Bean storing information about Bamboo servers etc.<p>
 * The class serves both as a configuration provider for plugin logic and Bean for persistence.
 */
public class JiraConfigurationBean extends AbstractConfigurationBean {

	private int pollTime = 1;

	private boolean displayIconDescription = false;

	public JiraConfigurationBean() {
        super();
    }

    public JiraConfigurationBean(ProductServerConfiguration cfg) {
		super(cfg);
		if (cfg instanceof JiraConfigurationBean) {
			this.pollTime = ((JiraConfigurationBean) cfg).getPollTime();
			this.displayIconDescription = ((JiraConfigurationBean) cfg).isDisplayIconDescription();
		}
	}

	public int getPollTime() {
		return pollTime;
	}

	public void setPollTime(int pollTime) {
		this.pollTime = pollTime;
	}

	public boolean isDisplayIconDescription() {
		return displayIconDescription;
	}

	public void setDisplayIconDescription(boolean displayIconDescription) {
		this.displayIconDescription = displayIconDescription;
	}
}