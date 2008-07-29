/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.configuration;

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.SubscribedPlan;

import java.util.ArrayList;

/**
 * Bean storing information about Bamboo servers etc.<p>
 * The class serves both as a configuration provider for plugin logic and Bean for persistence.
 */
public class BambooConfigurationBean extends AbstractServerConfigurationBean {

	private BambooTooltipOption bambooTooltipOption;
	private int pollTime = 1;
	
	public BambooConfigurationBean() {
        super();
    }

	public BambooConfigurationBean(BambooConfigurationBean cfg) {
        super(cfg);
        this.bambooTooltipOption = ((BambooConfigurationBean) cfg).getBambooTooltipOption();
        this.pollTime = ((BambooConfigurationBean) cfg).getPollTime();
    }

	@Override
	//@Transient
	public void storeServer(Server server) {
		Server foundServer = transientGetServer(server);
		if (foundServer == null) {
			servers.add((ServerBean) server);
		} else {
			foundServer.setName(server.getName());
			foundServer.transientSetPasswordString(server.transientGetPasswordString(), server.getShouldPasswordBeStored());
			foundServer.setUrlString(server.getUrlString());
			foundServer.setUserName(server.getUserName());
			foundServer.setEnabled(server.getEnabled());
			foundServer.setUseFavourite(server.getUseFavourite());			
			foundServer.transientSetSubscribedPlans(new ArrayList<SubscribedPlan>(server.transientGetSubscribedPlans()));
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
