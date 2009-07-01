package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.Constants;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class RunJiraActionGroup extends ActionGroup {
	private Map<Project, List<AnAction>> actions = new WeakHashMap<Project, List<AnAction>>();

	public void addAction(Project project, AnAction action) {
		if (!actions.containsKey(project)) {
			actions.put(project, new ArrayList<AnAction>());
		}
		actions.get(project).add(action);
	}

	public void clearActions(Project project) {
		if (actions.containsKey(project)) {
			actions.get(project).clear();
		}
	}

	@NotNull
	public AnAction[] getChildren(@Nullable final AnActionEvent anActionEvent) {
		if (anActionEvent != null) {
			Project project = anActionEvent.getData(DataKeys.PROJECT);
			if (project != null) {
				if (actions.containsKey(project)) {
					final List<AnAction> actionsList = actions.get(project);
					return actionsList.toArray(new AnAction[actionsList.size()]);
				}
			}
		}
		return new AnAction[]{};
	}

	@Override
	public void update(final AnActionEvent event) {
		boolean enabled = false;
		if (actions.size() > 0) {
			ServerData server = event.getData(Constants.SERVER_KEY);
			if (server != null && server.isEnabled()) {
				enabled = true;
			}
		}
		event.getPresentation().setEnabled(enabled);
	}
}
