package com.atlassian.theplugin.configuration;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 16, 2008
 * Time: 10:44:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class SubscribedPlanBean implements SubscribedPlan {
    private String planId;

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }
}
