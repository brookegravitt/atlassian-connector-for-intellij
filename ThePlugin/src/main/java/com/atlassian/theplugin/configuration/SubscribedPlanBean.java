package com.atlassian.theplugin.configuration;

public class SubscribedPlanBean implements SubscribedPlan {
	private String planId;

	public SubscribedPlanBean() {
	}

	public SubscribedPlanBean(SubscribedPlan cfg) {
		this.setPlanId(cfg.getPlanId());
	}

    public SubscribedPlanBean(String planId) {
        this.planId = planId;
    }

    public String getPlanId() {
		return planId;
	}

	public void setPlanId(String planId) {
		this.planId = planId;
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SubscribedPlanBean that = (SubscribedPlanBean) o;

		if (!planId.equals(that.planId)) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		return planId.hashCode();
	}
}
