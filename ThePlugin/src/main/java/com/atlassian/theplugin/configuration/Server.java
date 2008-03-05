package com.atlassian.theplugin.configuration;

import com.intellij.util.xmlb.annotations.Transient;

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

	String getUserName();

	Boolean getShouldPasswordBeStored();

	Boolean getIsConfigInitialized();

	Collection<SubscribedPlan> getSubscribedPlans();

	void setSubscribedPlans(Collection<? extends SubscribedPlan> subscribedPlans);

	String getPasswordString();

	Boolean getEnabled();

	Boolean getUseFavourite();

	void setName(String name);

	void setUrlString(String urlString);

	void setUserName(String anUsername);

	void setEnabled(Boolean enabled);

	void setUseFavourite(Boolean useFavourite);

	void setPasswordString(String aPassword, Boolean shouldBeStoredPermanently);

	void setUid(long uid);

	@Transient
	void setIsConfigInitialized(Boolean isConfigInitialized);
}
