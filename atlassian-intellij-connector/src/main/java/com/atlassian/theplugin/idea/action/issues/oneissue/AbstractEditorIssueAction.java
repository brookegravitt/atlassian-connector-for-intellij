package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.Constants;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public abstract class AbstractEditorIssueAction extends AnAction {
	@Override
	public void update(final AnActionEvent e) {
		boolean enabled = false;
		ServerData server = e.getData(Constants.SERVER_KEY);
		if (server != null && server.isEnabled()) {
			enabled = true;
		}
		e.getPresentation().setEnabled(enabled);
	}
}
