package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.bamboo.BambooStatusChecker;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-04-03
 * Time: 10:49:07
 * To change this template use File | Settings | File Templates.
 */
public class RefreshPanelAction extends AnAction {

	public void actionPerformed(AnActionEvent e) {
		ThePluginProjectComponent projectComponent = IdeaHelper.getCurrentProjectComponent(e);

		if (projectComponent != null) {
			BambooStatusChecker checker = projectComponent.getBambooStatusChecker();

			if (checker.canSchedule()) {
				new Thread(checker.newTimerTask()).start();
			}
		}
	}
}
