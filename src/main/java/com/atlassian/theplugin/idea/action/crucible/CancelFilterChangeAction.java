package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.IdeaHelper;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: May 30, 2008
 * Time: 11:41:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class CancelFilterChangeAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		if (IdeaHelper.getCrucibleToolWindowPanel(event) != null) {
			IdeaHelper.getCrucibleToolWindowPanel(event).cancelAdvancedFilter();
		}
	}
}
