package com.atlassian.theplugin.commons.bamboo;

import com.atlassian.theplugin.commons.Server;
import com.atlassian.theplugin.commons.util.DateUtil;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-05-28
 * Time: 11:47:32
 * To change this template use File | Settings | File Templates.
 */
public class BambooBuildAdapter {
	protected BambooBuild build;
	public static SimpleDateFormat BAMBOO_BUILD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

	public String getTestsPassedSummary() {
		if (getStatus() == BuildStatus.UNKNOWN) {
			return "-/-";
		} else {
			return getTestsPassed() + "/" + getTestsNumber();
		}
	}

	public Date getBuildTime() {
		return build.getBuildTime();
	}

	public String getBuildRelativeBuildDate() {
		return build.getBuildRelativeBuildDate() == null ? "" : build.getBuildRelativeBuildDate();
	}

	public String getBuildTimeFormated() {
	if (getBuildTime() != null) {
			return DateUtil.getRelativePastDate(new Date(), getBuildTime());
		} else {
			return "-";
		}
	}

	public Date getPollingTime() {
		return build.getPollingTime();
	}

	public String getBuildReason() {
		return build.getBuildReason() == null ? "" : build.getBuildReason();
	}
}
