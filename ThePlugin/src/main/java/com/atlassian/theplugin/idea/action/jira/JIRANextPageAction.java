package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: marek
 * Date: Apr 8, 2008
 * Time: 12:12:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class JIRANextPageAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		IdeaHelper.getJIRAToolWindowPanel(event).nextPage();
	}

	public void update(AnActionEvent event) {
		super.update(event);
		if (IdeaHelper.getJIRAToolWindowPanel(event) != null) {
			event.getPresentation().setEnabled(IdeaHelper.getJIRAToolWindowPanel(event).isNextPageAvailable());
		}
	}
}
