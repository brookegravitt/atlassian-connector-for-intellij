package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-04-03
 * Time: 11:30:12
 * To change this template use File | Settings | File Templates.
 */
public class RefreshPanelAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		ThePluginProjectComponent projectComponent = IdeaHelper.getCurrentProjectComponent(e);

		if (projectComponent != null) {
			CrucibleStatusChecker checker = projectComponent.getCrucibleStatusChecker();

			if (checker.canSchedule()) {
				new Thread(checker.newTimerTask()).start();
			}
		}
	}
}
