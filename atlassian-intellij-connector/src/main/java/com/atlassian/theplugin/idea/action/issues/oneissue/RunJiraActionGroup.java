package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RunJiraActionGroup extends ActionGroup {
	private List<AnAction> actions = new ArrayList<AnAction>();

	public void addAction(AnAction action) {
		actions.add(action);
	}

	public void clearActions() {
		actions.clear();
	}

	@NotNull
	public AnAction[] getChildren(@Nullable final AnActionEvent anActionEvent) {
		return actions.toArray(new AnAction[actions.size()]);
	}

	@Override
	public void update(final AnActionEvent anActionEvent) {
		boolean enabled = false;
		if (actions.size() > 0) {
			ServerCfg server = anActionEvent.getData(Constants.SERVER_KEY);
			if (server != null) {
				Project project = anActionEvent.getData(DataKeys.PROJECT);
				if (project != null) {
					ServerCfg server2 = IdeaHelper.getCfgManager()
							.getServer(CfgUtil.getProjectId(project), server.getServerId());
					if (server2 != null && server2.isEnabled()) {
						enabled = true;
					}
				}
			}
		}
		anActionEvent.getPresentation().setEnabled(enabled);
	}
}
