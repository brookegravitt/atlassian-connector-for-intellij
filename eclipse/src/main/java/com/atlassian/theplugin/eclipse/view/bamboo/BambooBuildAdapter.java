package com.atlassian.theplugin.eclipse.view.bamboo;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;

public class BambooBuildAdapter {

	private BambooBuild build;

	public BambooBuildAdapter(BambooBuild build) {
		this.build = build;
	}

	public String getBuildNumber() {
		return build.getBuildNumber();
	}

	public String getBuildKey() {
		return build.getBuildKey();
	}

	public String getProjectKey() {
		return build.getProjectKey();
	}

	public String getBuildReason() {
		return build.getBuildReason();
	}

	public String getMessage() {
		return build.getMessage();
	}

	public String getTestsPassedSummary() {
		return  build.getTestsPassed() + "/" + (build.getTestsPassed() + build.getTestsFailed());
	}

	public String getBuildTime() {
		return build.getBuildTime().toString();
	}

	public String getStatus() {
		return build.getStatus().toString();
	}

	public String getServerConfigName() {
		return build.getServer().getName();
	}

}
