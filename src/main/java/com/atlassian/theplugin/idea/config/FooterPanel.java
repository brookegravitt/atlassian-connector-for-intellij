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

package com.atlassian.theplugin.idea.config;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.HelpUrl;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.ui.HyperlinkLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class FooterPanel extends JPanel {
	private HyperlinkLabel openJiraHyperlinkBugLabel;
	private HyperlinkLabel openJiraHyperlinkStoryLabel;
	private HyperlinkLabel openConfigHelpLabel;

	private static final String BASE = "https://studio.atlassian.com/secure/CreateIssueDetails!init.jspa";
	private static final String PROJECT_ID = "10024";
	private static final String TICKET_TYPE_BUG = "1";
	private static final String TICKET_TYPE_STORY = "5";

	// TODO: this version list needs updating every time we add some new version to Jira.
	// This is all basically incorrect code and will break beyond 2.0, but whatever
	// - parsing Jira form source would be a ridiculous thing to do at this point
	private static Map<String, String> versionMap = new HashMap<String, String>();

	{
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

	public FooterPanel() {
		initLayout();
    }

	private void initLayout() {
		String versionName = PluginUtil.getInstance().getVersion();

		GridBagLayout gb = new GridBagLayout();
		setLayout(gb);

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
			environment +=
				"Java version=" + System.getProperty("java.version")
				+ ", Java vendor=" + System.getProperty("java.vendor")
				+ ", OS name=" + System.getProperty("os.name")
				+ ", OS architecture=" + System.getProperty("os.arch")
				+ ", IDEA build number=" + ApplicationInfo.getInstance().getBuildNumber();
			environment = URLEncoder.encode(environment, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LoggerImpl.getInstance().info(e);
		}

		final String bugUrl = BASE
				+ "?pid=" + PROJECT_ID
				+ "&versions=" + versionCodeForJira
				+ "&issuetype=" + TICKET_TYPE_BUG
				+ "&environment=" + environment;


		final String storyUrl = BASE
				+ "?pid=" + PROJECT_ID
				+ "&versions=" + versionCodeForJira
				+ "&issuetype=" + TICKET_TYPE_STORY;

		final String helpUrl = HelpUrl.getHelpUrl(Constants.HELP_CONFIG_PANEL);

		JPanel linkPanel = new JPanel();
		
		openJiraHyperlinkBugLabel = new HyperlinkLabel("Report Bug");
		openJiraHyperlinkBugLabel.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				BrowserUtil.launchBrowser(bugUrl);
			}
		});

		openJiraHyperlinkStoryLabel = new HyperlinkLabel("Request Feature");
		openJiraHyperlinkStoryLabel.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				BrowserUtil.launchBrowser(storyUrl);
			}
		});

		// jgorycki: in my and Marek's opinion, text-based link looks marginally less bad than an icon
		openConfigHelpLabel = new HyperlinkLabel("Help");
//		openConfigHelpLabel.setIcon(Constants.HELP_ICON);
//		openConfigHelpLabel.setToolTipText(helpUrl);

		openConfigHelpLabel.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				BrowserUtil.launchBrowser(helpUrl);
			}
		});

		linkPanel.add(openJiraHyperlinkBugLabel);
		linkPanel.add(new JLabel("|"));
		linkPanel.add(openJiraHyperlinkStoryLabel);
		linkPanel.add(new JLabel(versionName));
		
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.fill = GridBagConstraints.NONE;
		add(linkPanel, c);
		c.gridx = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(new JPanel(), c);
		c.gridx = 2;
		c.weightx = 0;
		c.anchor = GridBagConstraints.LINE_END;
		c.insets = new Insets(0, 0, 0, Constants.DIALOG_MARGIN);
		add(openConfigHelpLabel, c);
	}
}
