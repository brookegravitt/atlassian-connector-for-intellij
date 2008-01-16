package com.atlassian.theplugin.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 16, 2008
 * Time: 10:39:16 AM
 * To change this template use File | Settings | File Templates.
 */
public interface SubscribedPlan {
    Server getServer();
    String getPlanId();
}
