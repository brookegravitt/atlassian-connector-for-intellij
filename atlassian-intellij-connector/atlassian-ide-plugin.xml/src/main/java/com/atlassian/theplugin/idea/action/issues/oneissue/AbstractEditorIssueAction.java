package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;

public abstract class AbstractEditorIssueAction extends AnAction {
	@Override
	public void update(final AnActionEvent e) {
		boolean enabled = false;
		ServerCfg server = e.getData(Constants.SERVER_KEY);
		if (server != null) {
			Project project = e.getData(DataKeys.PROJECT);
			if (project != null) {
				ServerCfg server2 = IdeaHelper.getCfgManager().getServer(CfgUtil.getProjectId(project), server.getServerId());
				if (server2 != null && server2.isEnabled()) {
					enabled = true;
				}
			}
		}
		e.getPresentation().setEnabled(enabled);
	}
}
