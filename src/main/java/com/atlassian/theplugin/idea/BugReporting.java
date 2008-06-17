package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.util.PluginUtil;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.intellij.openapi.application.ApplicationInfo;

import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

public class BugReporting {

	// TODO: this version list needs updating every time we add some new version to Jira.
	// This is all basically incorrect code and will break beyond 2.0, but whatever
	// - parsing Jira form source would be a ridiculous thing to do at this point

	private static Map<String, String> versionMap = new HashMap<String, String>();

	static {
		versionMap.put("0", "10031"); // don't remove this entry or I will kill you
		versionMap.put("0.0.1", "10010");
		versionMap.put("0.1.0", "10011");
		versionMap.put("0.2.0", "10012");
		versionMap.put("0.3.1", "10013");
		versionMap.put("0.4.0", "10014");
		versionMap.put("0.5.0", "10015");
		versionMap.put("0.5.1", "10423");
		versionMap.put("1.0.0", "10016");
		versionMap.put("1.0.1", "10440");
		versionMap.put("1.1.0", "10017");
		versionMap.put("1.2.0", "10018");
		versionMap.put("1.2.1", "10470");
		versionMap.put("1.3.0", "10019");
		versionMap.put("1.3.1", "10472");
		versionMap.put("1.4.0", "10020");
		versionMap.put("1.4.1", "10482");
		versionMap.put("1.5.0", "10021");
		versionMap.put("2.0.0", "10022");
	}

	private static final String BASE = "https://studio.atlassian.com/secure/CreateIssueDetails!init.jspa";
	private static final String PROJECT_ID = "10024";
	private static final String TICKET_TYPE_BUG = "1";
	private static final String TICKET_TYPE_STORY = "5";
	private static String bugUrl;
	private static String storyUrl;
	private static String versionName;

	static {
		versionName = PluginUtil.getInstance().getVersion();
		// versions seen here are formatted:
		// "x.y.z-SNAPSHOT, SVN:ijk" or "x.y.z, SVN:ijk"
		// let's check for both possibilities
		int i = versionName.indexOf('-');
		if (i == -1) {
			i = versionName.indexOf(',');
		}

		String versionForJira;
		if (i != -1) {
			versionForJira = versionName.substring(0, i);
		} else {
			// this is going to suck and Jira is unlikely to find such a version, but
			// if we are here, this means that the version string is screwed up somehow
			// so whatever - it is b0rked anyway. let's pick "0" - it is the first pseudo-version
			// we have in Jira
			versionForJira = "0";
		}

		String versionCodeForJira = versionMap.get(versionForJira);
		if (versionCodeForJira == null) {
			// this is broken, but whatever. The user can always reselect the version manually. I hope :)
			versionCodeForJira = versionMap.get("0");
		}

		String environment = "";
		try {
			final String rawEnvironment =
				"Java version=" + System.getProperty("java.version")
				+ ", Java vendor=" + System.getProperty("java.vendor")
				+ ", OS name=" + System.getProperty("os.name")
				+ ", OS architecture=" + System.getProperty("os.arch")
				+ ", IDEA build number=" + ApplicationInfo.getInstance().getBuildNumber();
			environment = URLEncoder.encode(rawEnvironment, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LoggerImpl.getInstance().info(e);
		}

		bugUrl = BASE
				+ "?pid=" + PROJECT_ID
				+ "&versions=" + versionCodeForJira
				+ "&issuetype=" + TICKET_TYPE_BUG
				+ "&environment=" + environment;


		storyUrl = BASE
				+ "?pid=" + PROJECT_ID
				+ "&versions=" + versionCodeForJira
				+ "&issuetype=" + TICKET_TYPE_STORY;
	}

	public static String getBugUrl() {
		return bugUrl;
	}

	public static String getStoryUrl() {
		return storyUrl;
	}

	public static String getVersionString() {
		return versionName;
	}

	public static String getBugWithDescriptionUrl(String description) {
		try {
			return bugUrl + "&description=" + URLEncoder.encode(description, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return bugUrl;
		}
	}

	private BugReporting() {
	}
}
