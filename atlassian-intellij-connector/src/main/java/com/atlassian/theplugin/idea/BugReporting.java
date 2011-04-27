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

package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.util.UrlUtil;
import com.atlassian.theplugin.util.PluginUtil;

import java.util.HashMap;
import java.util.Map;

public final class BugReporting {

	// TODO: this version list needs updating every time we add some new version to Jira.
	// This is all basically incorrect code and will break beyond 2.0, but whatever
	// - parsing Jira form source would be a ridiculous thing to do at this point

	private static Map<String, String> versionMap = new HashMap<String, String>();

	static {
		versionMap.put("1.0-alpha", "12482");
	}

    private static final int MAX_URI_LENGTH = 2048;
	private static final String BASE = "https://studio.atlassian.com/secure/CreateIssueDetails!init.jspa";
	private static final String PROJECT_ID = "10770";
	private static final String TICKET_TYPE_BUG = "1";
	private static final String TICKET_TYPE_STORY = "5";
	private static String storyUrl;
	private static final String VERSION_NAME;
    private static String versionCodeForJira;

    static {
		VERSION_NAME = PluginUtil.getInstance().getVersion();
		// versions seen here are formatted:
		// "x.y.z-SNAPSHOT, SVN:ijk" or "x.y.z, SVN:ijk"
		// let's check for both possibilities
		int i = VERSION_NAME.indexOf("-SNAPSHOT");
		if (i == -1) {
			i = VERSION_NAME.indexOf(',');
		}

		String versionForJira;
		if (i != -1) {
			versionForJira = VERSION_NAME.substring(0, i);
		} else {
			// this is going to suck and Jira is unlikely to find such a version, but
			// if we are here, this means that the version string is screwed up somehow
			// so whatever - it is b0rked anyway. let's pick "0" - it is the first pseudo-version
			// we have in Jira
			versionForJira = "0";
		}

        versionCodeForJira = versionMap.get(versionForJira);
		if (versionCodeForJira == null) {
			// this is broken, but whatever. The user can always reselect the version manually. I hope :)
			versionCodeForJira = versionMap.get("0");
		}

		storyUrl = BASE
				+ "?pid=" + PROJECT_ID
				+ "&versions=" + versionCodeForJira
				+ "&issuetype=" + TICKET_TYPE_STORY;
	}

	public static String getBugUrl(String ideaBuildNumber) {
        final String rawEnvironment =
                "Java version=" + System.getProperty("java.version")
                        + ", Java vendor=" + System.getProperty("java.vendor")
                        + ", OS name=" + System.getProperty("os.name")
                        + ", OS architecture=" + System.getProperty("os.arch")
                        + ", IDEA build number=" + (ideaBuildNumber != null ? ideaBuildNumber : "unknown")
						+ ", Plugin version=" + VERSION_NAME;
        final String environment = UrlUtil.encodeUrl(rawEnvironment);

        return BASE
                + "?pid=" + PROJECT_ID
                + "&versions=" + versionCodeForJira
                + "&issuetype=" + TICKET_TYPE_BUG
                + "&environment=" + environment;
	}

	public static String getStoryUrl() {
		return storyUrl;
	}

	public static String getVersionString() {
		return VERSION_NAME;
	}

    public static String getBugWithDescriptionUrl(String ideaBuildNumber, String description) {
        final String urlStart = getBugUrl(ideaBuildNumber) + "&description=";
        final int charsLeft = MAX_URI_LENGTH - urlStart.length();
        return urlStart + getBoundedEncodedString(description, charsLeft);
    }

    static String getBoundedEncodedString(String description, int maxLen) {
        String encoded = UrlUtil.encodeUrl(description);
        while (encoded.length() > maxLen) {
            int lastNewline = description.lastIndexOf('\n');
            if (lastNewline == -1) {
                return "";
            }
            description = description.substring(0, lastNewline);
            encoded = UrlUtil.encodeUrl(description);
        }

        return encoded;

    }

    private BugReporting() {
	}
}
