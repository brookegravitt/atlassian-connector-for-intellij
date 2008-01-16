package com.atlassian.theplugin.configuration;

import java.util.Collection;

/**
 * Bamboo server configuration.
 * User: sginter
 * Date: Jan 10, 2008
 * Time: 12:04:40 PM
 */
public interface Server {
    String getName();
    String getUrlString();
    String getUsername();
    String getPassword();

    Collection<? extends SubscribedPlan> getSubscribedPlans();
}
