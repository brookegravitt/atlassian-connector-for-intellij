package com.atlassian.theplugin.idea.action;

import com.atlassian.theplugin.idea.Constants;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class HelpAction extends AnAction {

	private String topic;

	public HelpAction(String helpTopic) {
		topic = Constants.HELP_URL_BASE + helpTopic;
	}

	public void actionPerformed(AnActionEvent event) {
		BrowserUtil.launchBrowser(topic);
	}
}
