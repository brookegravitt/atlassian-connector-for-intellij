package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;

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
		ServerCfg server = event.getData(Constants.SERVER_KEY);
		if (server != null) {
			Project project = event.getData(DataKeys.PROJECT);
			if (project != null) {
				ServerCfg server2 = IdeaHelper.getCfgManager().getServer(CfgUtil.getProjectId(project), server.getServerId());
				if (server2 != null && server2.isEnabled()) {
					if (ModelFreezeUpdater.getState(event)) {
						enabled = true;
					}
				}
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
