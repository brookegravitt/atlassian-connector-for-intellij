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

	public String getServerName() {
		if (build.getServer() != null) {
			return build.getServer().getName() == null ? "" : build.getServer().getName();
		} else {
			return "";
		}
	}

	public boolean isBamboo2() {
		if (build.getServer() != null) {
			return build.getServer().isBamboo2();
		} else {
			return false;
		}
	}

	public String getServerUrl() {
		return build.getServerUrl() == null ? "" : build.getServerUrl();
	}

	public String getProjectName() {
		return build.getProjectName() == null ? "" : build.getProjectName();
	}

	public String getProjectKey() {
		return build.getProjectKey() == null ? "" : build.getProjectKey();
	}

	public String getProjectUrl() {
		return build.getProjectUrl() == null ? "" : build.getProjectUrl();
	}

	public String getBuildUrl() {
		return build.getBuildUrl() == null ? "" : build.getBuildUrl();
	}

	public String getBuildName() {
		return build.getBuildName() == null ? "" : build.getBuildName();
	}

	public String getBuildKey() {
		return build.getBuildKey() == null ? "" : build.getBuildKey();
	}

	public boolean getEnabled() {
		return build.getEnabled();
	}

	public String getBuildNumber() {
		return build.getBuildNumber() == null ? "0" : build.getBuildNumber();
	}

	public String getBuildResultUrl() {
		return build.getBuildResultUrl() == null ? "" : build.getBuildResultUrl();
	}

	public BuildStatus getStatus() {
		return build.getStatus();
	}

	public String getMessage() {
		return build.getMessage() == null ? "" : build.getMessage();
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
		return build.getBuildRelativeBuildDate() == null ? "" : build.getBuildRelativeBuildDate();
	}

	public Date getPollingTime() {
		return build.getPollingTime();
	}

	public Icon getBuildIcon() {
		if (build.getEnabled()) {
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
		} else {
			return ICON_GREY;
		}
	}

	public String getBuildReason() {
		return build.getBuildReason() == null ? "" : build.getBuildReason();
	}
}
