package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.commons.jira.api.JIRAIssue;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.Constants;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: pmaruszak
 */
public abstract class JIRAAbstractAction extends AnAction {
	public abstract void onUpdate(AnActionEvent event);

	public void onUpdate(AnActionEvent event, boolean enabled) {
	}


	@Override
	public final void update(AnActionEvent event) {
		super.update(event);
//
//		boolean enabled = ModelFreezeUpdater.getState(event);
//
//		if (enabled) {
//			onUpdate(event);
//		}
//		onUpdate(event, enabled);

		boolean enabled = false;
		ServerData server = null;
		JIRAIssue selectedIssue = event.getData(Constants.ISSUE_KEY);
		if (selectedIssue != null) {
			server = selectedIssue.getServer();
		}
		if (server == null && event.getData(Constants.SERVER_KEY) != null) {
			server = event.getData(Constants.SERVER_KEY);
		}
		if (server != null && server.isEnabled()) {
			if (ModelFreezeUpdater.getState(event)) {
				enabled = true;
			}
		}
		event.getPresentation().setEnabled(enabled);

//		enabled = ModelFreezeUpdater.getState(event);

		if (enabled) {
			onUpdate(event);
		}
		onUpdate(event, enabled);
	}
}
