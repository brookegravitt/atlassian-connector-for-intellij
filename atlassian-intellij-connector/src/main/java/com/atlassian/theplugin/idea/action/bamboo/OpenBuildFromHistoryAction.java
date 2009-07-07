package com.atlassian.theplugin.idea.action.bamboo;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel;
import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

/**
 * User: kalamon
 * Date: Jul 6, 2009
 * Time: 4:11:08 PM
 */
public class OpenBuildFromHistoryAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        final BambooToolWindowPanel buildsWindow = IdeaHelper.getBambooToolWindowPanel(event);
        if (buildsWindow != null) {
            BambooBuild build = buildsWindow.getSelectedHistoryBuild();
            if (build != null) {
                buildsWindow.openBuild(new BambooBuildAdapterIdea(build));
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
