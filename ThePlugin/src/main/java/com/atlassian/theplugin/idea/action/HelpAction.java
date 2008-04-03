package com.atlassian.theplugin.idea.action;

import com.atlassian.theplugin.idea.HelpUrl;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class HelpAction extends AnAction {

	private String url;

	public HelpAction(String helpTopic) {
		url = HelpUrl.getHelpUrl(helpTopic);
	}

	public void actionPerformed(AnActionEvent event) {
		BrowserUtil.launchBrowser(url);
	}
}
