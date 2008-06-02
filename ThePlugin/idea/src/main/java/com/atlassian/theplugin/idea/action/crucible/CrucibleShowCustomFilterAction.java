package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.jira.JIRAToolWindowPanel;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.CrucibleTableToolWindowPanel;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: May 30, 2008
 * Time: 9:47:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleShowCustomFilterAction extends AnAction {

	public void actionPerformed(AnActionEvent e) {
		final CrucibleTableToolWindowPanel cruciblePanel = IdeaHelper.getCrucibleToolWindowPanel(e);

		if (cruciblePanel != null) {
			cruciblePanel.showCrucibleCustomFilter();
		}
	}

	public void update(AnActionEvent event) {
		super.update(event);

		//if (IdeaHelper.getCrucibleToolWindowPanel((event) != null) {

//			if (IdeaHelper.getCrucibleToolWindowPanel(event).getFilters().getSavedFilterUsed()) {
//				event.getPresentation().setEnabled(false);
//			} else {
//				event.getPresentation().setEnabled(true);
//			}
//		} else {
//			event.getPresentation().setEnabled(false);
//		}

	}
}
