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

package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.commons.crucible.CrucibleVersion;
import com.atlassian.theplugin.commons.crucible.api.PredefinedFilter;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;

public class PredefinedFilterAction extends Crucible16ToggleAction {
    protected PredefinedFilter filter;

    public PredefinedFilterAction(PredefinedFilter filter) {
        this.filter = filter;
    }

    private Boolean[] getPredefinedFilters(AnActionEvent event) {
        Project project = IdeaHelper.getCurrentProject(event);
        if (project != null) {
            ThePluginProjectComponent projectComponent = IdeaHelper.getCurrentProject(event).getComponent(ThePluginProjectComponent.class);
            ProjectConfigurationBean projectConfiguration = projectComponent.getProjectConfigurationBean();
            return projectConfiguration.getCrucibleConfiguration().getCrucibleFilters().getPredefinedFilters();
        }
        return null;
    }

    public boolean isSelected(AnActionEvent event) {
        Boolean[] filters = getPredefinedFilters(event);
        if (filters != null) {
            if (filters[filter.ordinal()] == null) {
                filters[filter.ordinal()] = false;
            }
            return filters[filter.ordinal()];
        } else {
            return false;
        }
    }

    public void setSelected(AnActionEvent event, boolean b) {
        Boolean[] filters = getPredefinedFilters(event);
        if (filters != null) {
            filters[filter.ordinal()] = b;
            IdeaHelper.getCrucibleToolWindowPanel(event).showPredefinedFilter(filter, b);
        }        
    }
}