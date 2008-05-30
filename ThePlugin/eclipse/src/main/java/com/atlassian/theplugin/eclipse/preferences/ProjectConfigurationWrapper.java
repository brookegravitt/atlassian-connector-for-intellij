/**
 * 
 */
package com.atlassian.theplugin.eclipse.preferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Preferences;

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.configuration.BambooConfigurationBean;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.commons.configuration.SubscribedPlanBean;

/**
 * @author Jacek
 *
 */
public class ProjectConfigurationWrapper {
	
	private Preferences preferences;

	public ProjectConfigurationWrapper(Preferences preferences) {
		this.preferences = preferences;
	}

	public PluginConfigurationBean getPluginConfiguration() {
		
		List<SubscribedPlanBean> subscribedPlans = new ArrayList<SubscribedPlanBean>();
		
		String[] plans = preferences.getString(PreferenceConstants.BAMBOO_BUILDS).split(" ");
		
		for (String plan : plans) {
			if (plan != null && !plan.isEmpty()) {
				SubscribedPlanBean subscribedPlan = new SubscribedPlanBean(plan);
				subscribedPlans.add(subscribedPlan);
			}
		}
		
		
		ServerBean bambooServer = new ServerBean();
		bambooServer.setEnabled(true);
		bambooServer.setUserName(preferences.getString(PreferenceConstants.BAMBOO_USER_NAME));
		bambooServer.transientSetPasswordString(preferences.getString(PreferenceConstants.BAMBOO_USER_PASSWORD), true);
		bambooServer.setUrlString(preferences.getString(PreferenceConstants.BAMBOO_URL));
		bambooServer.setSubscribedPlansData(subscribedPlans);

		Collection<Server> bambooServers = new ArrayList<Server>();
		bambooServers.add(bambooServer);
		
		BambooConfigurationBean bambooConfiguration = new BambooConfigurationBean();
		bambooConfiguration.setServers(bambooServers);
		
		PluginConfigurationBean pluginConfiguration = new PluginConfigurationBean();
		pluginConfiguration.setBambooConfigurationData(bambooConfiguration);
		
		return pluginConfiguration;
	}

}
