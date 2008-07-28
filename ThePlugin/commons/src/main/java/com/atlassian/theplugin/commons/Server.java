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

package com.atlassian.theplugin.commons;


import java.util.Collection;

/**
 * Bamboo server configuration.
 * @author sginter
 */
public interface Server {
	long getUid();

	String getName();

	String getUrlString();

	String getUserName();

	Boolean getShouldPasswordBeStored();

	Boolean getIsConfigInitialized();

	Collection<SubscribedPlan> transientGetSubscribedPlans();

	void transientSetSubscribedPlans(Collection<SubscribedPlan> subscribedPlans);

	String transientGetPasswordString();

	Boolean getEnabled();

	Boolean getUseFavourite();

	void setName(String name);

	void setUrlString(String urlString);

	void setUserName(String anUsername);

	void setEnabled(Boolean enabled);

	void setUseFavourite(Boolean useFavourite);

	void transientSetPasswordString(String aPassword, Boolean shouldBeStoredPermanently);

	void setUid(long uid);

	// Transient
	void transientSetIsConfigInitialized(Boolean isConfigInitialized);

	// Transient
	boolean isBamboo2();

	void setIsBamboo2(boolean bamboo2);
}
