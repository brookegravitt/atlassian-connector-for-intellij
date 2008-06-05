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
import com.atlassian.theplugin.commons.crucible.api.CustomFilterData;
import com.atlassian.theplugin.configuration.ProjectConfigurationBean;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;

public class CustomFilterAction extends ToggleAction {
    public CustomFilterAction() {
    }

    private CustomFilterData getFilter(AnActionEvent event) {
        Project project = IdeaHelper.getCurrentProject(event);
        if (project != null) {
            ThePluginProjectComponent projectComponent = IdeaHelper.getCurrentProject(event).getComponent(ThePluginProjectComponent.class);
            ProjectConfigurationBean projectConfiguration = projectComponent.getProjectConfigurationBean();
            if (!projectConfiguration.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().isEmpty()) {
                for (String s : projectConfiguration.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().keySet()) {
                    CustomFilterData filter = projectConfiguration.getCrucibleConfiguration().getCrucibleFilters().getManualFilter().get(s);
                    return filter;
                }
            }
        }
        return null;
    }

    public boolean isSelected(AnActionEvent event) {
        CustomFilterData filter = getFilter(event);
        if (filter != null) {
            return filter.isEnabled();
        }
        return false;
    }

    public void setSelected(AnActionEvent event, boolean b) {
        CustomFilterData filter = getFilter(event);
        if (filter != null) {
            filter.setEnabled(b);
            IdeaHelper.getCrucibleToolWindowPanel(event).showCustomFilter(b);
        }       
    }

    public void update(AnActionEvent event) {
        super.update(event);
        if (IdeaHelper.getCrucibleToolWindowPanel(event) != null) {
            event.getPresentation().setVisible(
                    (IdeaHelper.getCrucibleToolWindowPanel(event).getCrucibleVersion() == CrucibleVersion.CRUCIBLE_16));
        } else {
            event.getPresentation().setVisible(false);
        }
    }
}