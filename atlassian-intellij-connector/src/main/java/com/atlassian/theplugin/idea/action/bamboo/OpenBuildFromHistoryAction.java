package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * User: kalamon
 * Date: Jul 6, 2009
 * Time: 4:11:08 PM
 */
public class OpenBuildFromHistoryAction extends AnAction {
    @Override
	public void actionPerformed(AnActionEvent event) {
        final BambooToolWindowPanel buildsWindow = IdeaHelper.getBambooToolWindowPanel(event);
        if (buildsWindow != null) {
			BambooBuildAdapter build = buildsWindow.getSelectedHistoryBuild();
            if (build != null) {
				buildsWindow.openBuild(build);
            }
        }
    }

    @Override
    public void update(AnActionEvent event) {

        boolean enabled = false;
        final BambooToolWindowPanel buildsWindow = IdeaHelper.getBambooToolWindowPanel(event);
        if (buildsWindow != null) {
            enabled = buildsWindow.getSelectedHistoryBuild() != null;
        }
        event.getPresentation().setEnabled(enabled);
    }
}
