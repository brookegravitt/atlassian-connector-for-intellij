package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilterBean;
import com.atlassian.theplugin.configuration.CrucibleWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.atlassian.theplugin.idea.crucible.CrucibleCustomFilterDialog;
import com.atlassian.theplugin.idea.crucible.ReviewListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * @author pmaruszak
 */
public class EditManualFilterAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
        final Project project = IdeaHelper.getCurrentProject(event);
        final ProjectCfgManagerImpl projectCfgManager = IdeaHelper.getProjectCfgManager(project);
        final ReviewListToolWindowPanel reviewListToolWindowPanel = IdeaHelper.getReviewListToolWindowPanel(project);
        final CrucibleWorkspaceConfiguration projectCrucibleCfg = reviewListToolWindowPanel.getCrucibleConfiguration();
        final CustomFilterBean filter = projectCrucibleCfg.getCrucibleFilters().getManualFilter();
        final UiTaskExecutor uiTaskExecutor = IdeaHelper.getReviewListToolWindowPanel(project).getUiTaskExecutor();
        
        final CrucibleCustomFilterDialog dialog = new CrucibleCustomFilterDialog(
                project, projectCfgManager, projectCrucibleCfg.getCrucibleFilters().getManualFilter(),
                uiTaskExecutor);

        dialog.show();

        if (dialog.getExitCode() == 0 && dialog.getFilter() != null) {
            final CustomFilterBean newFilter = dialog.getFilter();
            projectCrucibleCfg.getCrucibleFilters().setManualFilter(newFilter);
            // refresh reviews panel
            reviewListToolWindowPanel.notifyCrucibleFilterListModelListeners(newFilter);
        }
    }

}