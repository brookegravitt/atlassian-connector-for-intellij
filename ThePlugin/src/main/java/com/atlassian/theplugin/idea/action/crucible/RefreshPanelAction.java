package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.Logger;
import com.atlassian.theplugin.idea.ProgressAnimationProvider;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
import com.atlassian.theplugin.util.PluginUtil;
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
			final CrucibleStatusChecker checker = projectComponent.getCrucibleStatusChecker();

			if (checker.canSchedule()) {

				final ProgressAnimationProvider animator =
								IdeaHelper.getCrucibleToolWindowPanel(e).getProgressAnimation();

				final Logger log = PluginUtil.getLogger();

				new Thread(new Runnable() {
					public void run() {

						Thread t = new Thread(checker.newTimerTask(), "Manual Crucible panel refresh (checker)");

						animator.startProgressAnimation();

						t.start();
						try {
							t.join();
						} catch (InterruptedException e) {
							log.warn(e.toString());
						} finally {
							animator.stopProgressAnimation();
						}


					}
				}, "Manual Crucible panel refresh").start();

			}
		}
	}
}
