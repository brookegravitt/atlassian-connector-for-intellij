package com.atlassian.theplugin.idea.action.fisheye;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.Change;

import java.util.Collection;

public abstract class AbstractFisheyeAction extends AnAction {

	public void update(final AnActionEvent event) {
		CrucibleServerCfg crucibleServerCfg = getCrucibleServerCfg(event);
		if (crucibleServerCfg != null) {
			if (crucibleServerCfg.isFisheyeInstance()) {
				if (crucibleServerCfg.getProjectName() != null
						&& crucibleServerCfg.getRepositoryName() != null) {
					event.getPresentation().setVisible(true);
					return;
				}
			}
		}
		event.getPresentation().setVisible(false);
	}

	protected CrucibleServerCfg getCrucibleServerCfg(final AnActionEvent event) {
		Project project = IdeaHelper.getCurrentProject(event);
		if (project != null) {
			Collection<ServerCfg> servers = IdeaHelper.getCfgManager().getProjectSpecificServers(CfgUtil.getProjectId(project));
			for (ServerCfg server : servers) {
				if (server.getServerType().equals(ServerType.CRUCIBLE_SERVER) && server.isEnabled() && server.isComplete()) {
					return (CrucibleServerCfg) server;
				}
			}
		}
		return null;
	}

	protected String getRevision(AnActionEvent event) {
		final ChangeList[] changes = DataKeys.CHANGE_LISTS.getData(event.getDataContext());
		if (changes != null && changes.length == 1) {
			for (Change change : changes[0].getChanges()) {
				if (change.getAfterRevision() == null) {
					continue;
				}
				return change.getAfterRevision().getRevisionNumber().asString();
			}
		}
		return null;
	}
}