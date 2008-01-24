package com.atlassian.theplugin.configuration;

import java.util.Collection;

/**
 * Bamboo server configuration.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 12:04:40 PM
 */
public interface Server extends Cloneable {
    String getName();
    String getUrlString();
    String getUsername();
    String getEncryptedPassword();
    Boolean getShouldPasswordBeStored();
    Boolean getIsConfigInitialized();

    Collection<? extends SubscribedPlan> getSubscribedPlans();

    String getPasswordString() throws ServerPasswordNotProvidedException;
}
