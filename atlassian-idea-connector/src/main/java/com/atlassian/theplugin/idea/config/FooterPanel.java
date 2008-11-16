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

import com.atlassian.theplugin.idea.BugReporting;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.HelpUrl;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.ui.HyperlinkLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

public class FooterPanel extends JPanel {


	public FooterPanel() {
		initLayout();
    }

	private void initLayout() {

		GridBagLayout gb = new GridBagLayout();
		setLayout(gb);


		final String helpUrl = HelpUrl.getHelpUrl(Constants.HELP_CONFIG_PANEL);

		JPanel linkPanel = new JPanel();

		final HyperlinkLabel openJiraHyperlinkBugLabel = new HyperlinkLabel("Report Bug");
		openJiraHyperlinkBugLabel.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				BrowserUtil.launchBrowser(BugReporting.getBugUrl(ApplicationInfo.getInstance().getBuildNumber()));
			}
		});

		final HyperlinkLabel openJiraHyperlinkStoryLabel = new HyperlinkLabel("Request Feature");
		openJiraHyperlinkStoryLabel.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				BrowserUtil.launchBrowser(BugReporting.getStoryUrl());
			}
		});

		// jgorycki: in my and Marek's opinion, text-based link looks marginally less bad than an icon
		final HyperlinkLabel openConfigHelpLabel = new HyperlinkLabel("Help");
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
		linkPanel.add(new JLabel(BugReporting.getVersionString()));
		
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
