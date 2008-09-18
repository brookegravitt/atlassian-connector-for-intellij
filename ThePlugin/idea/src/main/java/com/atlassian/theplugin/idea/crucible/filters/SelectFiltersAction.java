/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.crucible.filters;

import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.crucible.CrucibleFiltersBean;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.idea.action.crucible.Crucible16ToggleAction;
import com.atlassian.theplugin.idea.crucible.CrucibleStatusChecker;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;

public class SelectFiltersAction extends AnAction {
    protected PredefinedFilter filter;

    public SelectFiltersAction() {
    }

	public void actionPerformed(final AnActionEvent e) {
		IdeaHelper.getCrucibleToolWindowPanel(e).showSelectFilterDialog();
	}

	private CrucibleFiltersBean getCrucibleFilters(AnActionEvent event) {
        Project project = IdeaHelper.getCurrentProject(event);
        if (project != null) {
            ThePluginProjectComponent projectComponent
                    = IdeaHelper.getCurrentProject(event).getComponent(ThePluginProjectComponent.class);
            ProjectConfigurationBean projectConfiguration = projectComponent.getProjectConfigurationBean();
            return projectConfiguration.getCrucibleConfiguration().getCrucibleFilters();
        }
        return null;
    }



	public boolean isSelected(AnActionEvent event) {
        CrucibleFiltersBean config = getCrucibleFilters(event);
        if (config != null) {
			Boolean[] filters = config.getPredefinedFilters();
			if (filters[filter.ordinal()] == null) {
                filters[filter.ordinal()] = false;
            }
            return filters[filter.ordinal()];
        } else {
            return false;
        }
    }

    public void setSelected(AnActionEvent event, boolean b) {
		CrucibleFiltersBean config = getCrucibleFilters(event);
        if (config != null) {
			Boolean[] filters = config.getPredefinedFilters();
			filters[filter.ordinal()] = b;

			CrucibleStatusChecker checker = null;
            if (b) {
                ThePluginProjectComponent projectComponent = IdeaHelper.getCurrentProjectComponent(event);
                if (projectComponent != null) {
                    checker = projectComponent.getCrucibleStatusChecker();
                }
            }
			IdeaHelper.getCrucibleToolWindowPanel(event).showPredefinedFilter(filter, b, checker);
        }
    }
}