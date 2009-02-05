package com.atlassian.theplugin.idea.action.issues.oneissue;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
		anActionEvent.getPresentation().setEnabled(actions.size() > 0);
	}
}
