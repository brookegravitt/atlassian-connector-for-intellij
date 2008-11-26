package com.atlassian.theplugin.idea.action.issues;

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

		boolean enabled = ModelFreezeUpdater.getStateAndSetPresentationEnabled(event);
		
		if (enabled) {

			onUpdate(event);
		}

		onUpdate(event, enabled);
	}
}
