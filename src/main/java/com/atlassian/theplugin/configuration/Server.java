package com.atlassian.theplugin.configuration;

import java.util.Collection;

/**
 * Bamboo server configuration.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 12:04:40 PM
 */
public interface Server {
	long getUid();

	String getName();

	String getUrlString();

	String getUsername();

	Boolean getShouldPasswordBeStored();

	Boolean getIsConfigInitialized();

	Collection<? extends SubscribedPlan> getSubscribedPlans();

	String getPasswordString();

	Boolean getEnabled();
}
