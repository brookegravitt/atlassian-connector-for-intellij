package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.annotations.Transient;

import java.util.ArrayList;

/**
 * Bean storing information about Bamboo servers etc.<p>
 * The class serves both as a configuration provider for plugin logic and Bean for persistence.
 */
public class BambooConfigurationBean extends AbstractConfigurationBean {

	private BambooTooltipOption bambooTooltipOption;
	private int pollTime = 1;
	
	public BambooConfigurationBean() {
        super();
    }

	public BambooConfigurationBean(ProductServerConfiguration cfg) {
		super(cfg);
		if (cfg instanceof BambooConfigurationBean) {
			this.bambooTooltipOption = ((BambooConfigurationBean) cfg).getBambooTooltipOption();
			this.pollTime = ((BambooConfigurationBean) cfg).getPollTime();
		}
	}

	@Override
	@Transient
	public void storeServer(Server server) {
		Server foundServer = getServer(server);
		if (foundServer == null) {
			servers.add((ServerBean) server);
		} else {
			foundServer.setName(server.getName());
			foundServer.setPasswordString(server.getPasswordString(), server.getShouldPasswordBeStored());
			foundServer.setUrlString(server.getUrlString());
			foundServer.setUserName(server.getUserName());
			foundServer.setEnabled(server.getEnabled());
			foundServer.setUseFavourite(server.getUseFavourite());			
			foundServer.setSubscribedPlans(new ArrayList<SubscribedPlan>(server.getSubscribedPlans()));
		}
	}

	public int getPollTime() {
		return pollTime;
	}

	public void setPollTime(int pollTime) {
		this.pollTime = pollTime;
	}

	public BambooTooltipOption getBambooTooltipOption() {
		return bambooTooltipOption;
	}

	public void setBambooTooltipOption(BambooTooltipOption bambooTooltipOption) {
		this.bambooTooltipOption = bambooTooltipOption;
	}
}
