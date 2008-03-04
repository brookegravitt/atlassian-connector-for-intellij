package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.bamboo.BambooPlan;

public class BambooPlanItem {
	private BambooPlan plan;
	private boolean selected;

	public BambooPlanItem(BambooPlan plan, boolean selected) {
		this.plan = plan;
		this.selected = selected;
	}

	public BambooPlan getPlan() {
		return plan;
	}

	public void setPlan(BambooPlan plan) {
		this.plan = plan;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
