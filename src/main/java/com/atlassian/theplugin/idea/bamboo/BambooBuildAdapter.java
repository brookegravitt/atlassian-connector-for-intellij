package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.bamboo.BambooBuild;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.configuration.Server;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.util.Date;

public class BambooBuildAdapter {
	private static final Icon ICON_RED = IconLoader.getIcon("/icons/icn_plan_failed.gif");
	private static final Icon ICON_GREEN = IconLoader.getIcon("/icons/icn_plan_passed.gif");
	private static final Icon ICON_GREY = IconLoader.getIcon("/icons/icn_plan_disabled.gif");

	private BambooBuild build;

	public BambooBuildAdapter(BambooBuild build) {
		this.build = build;
	}

	public Server getServer() {
		return build.getServer();
	}

	public String getServerUrl() {
		return build.getServerUrl();
	}

	public String getProjectName() {
		return build.getProjectName();
	}

	public String getProjectKey() {
		return build.getProjectKey() == null ? "" : build.getProjectKey(); 
	}

	public String getProjectUrl() {
		return build.getProjectUrl();
	}

	public String getBuildUrl() {
		return build.getBuildUrl();
	}

	public String getBuildName() {
		return build.getBuildName();
	}

	public String getBuildKey() {
		return build.getBuildKey();
	}

	public boolean getEnabled() {
		return build.getEnabled();
	}

	public String getBuildNumber() {
		return build.getBuildNumber();
	}

	public String getBuildResultUrl() {
		return build.getBuildResultUrl();
	}

	public BuildStatus getStatus() {
		return build.getStatus();
	}

	public String getMessage() {
		return build.getMessage();
	}

	public int getTestsPassed() {
		return build.getTestsPassed();
	}

	public int getTestsFailed() {
		return build.getTestsFailed();
	}

	public int getTestsNumber() {
		return build.getTestsPassed() + build.getTestsFailed();
	}

	public Date getBuildTime() {
		return build.getBuildTime();
	}

	public String getBuildRelativeBuildDate() {
		return build.getBuildRelativeBuildDate();
	}

	public Date getPollingTime() {
		return build.getPollingTime();
	}

	public Icon getBuildIcon() {
		switch (build.getStatus()) {
			case BUILD_FAILED:
				return ICON_RED;
			case UNKNOWN:
				return ICON_GREY;
			case BUILD_SUCCEED:
				return ICON_GREEN;
			default:
				throw new IllegalArgumentException("Illegal state of build.");
		}
	}

	public String getBuildReason() {
		return build.getBuildReason();
	}
}
