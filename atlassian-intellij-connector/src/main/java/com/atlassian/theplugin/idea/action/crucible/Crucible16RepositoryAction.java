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

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ProjectId;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.action.fisheye.ChangeListUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;


public abstract class Crucible16RepositoryAction extends AnAction {
//	@Override
//	public void update2(AnActionEvent event) {
//
//		final boolean isCrucibleConfigured = isCrucibleConfigured(event);
//		event.getPresentation().setVisible(isCrucibleConfigured);
//
//		if (isCrucibleConfigured) {
//			event.getPresentation().setEnabled(ChangeListUtil.getRevision(event) != null);
//		}
//	}

	@Override
	public void update(AnActionEvent event) {
		final boolean configured = isAnyCrucibleConfigured(event);
		event.getPresentation().setVisible(configured);

		if (configured) {
			event.getPresentation().setEnabled(ChangeListUtil.getRevision(event) != null);
		}
	}


	protected boolean isCrucibleConfigured(final AnActionEvent event) {
		final Project project = IdeaHelper.getCurrentProject(event);
		if (project == null) {
			return false;
		}

		final ProjectId projectId = CfgUtil.getProjectId(project);
		final ProjectConfiguration projectCfg = IdeaHelper.getCfgManager().getProjectConfiguration(projectId);
		if (projectCfg == null) {
			return false;
		}

		if (projectCfg.getDefaultCrucibleServer() == null /*|| projectCfg.getDefaultCrucibleProject() == null*/) {
			return false;
		}

		return true;
	}

	protected boolean isAnyCrucibleConfigured(final AnActionEvent event) {
		final Project project = IdeaHelper.getCurrentProject(event);
		if (project == null) {
			return false;
		}


		final ProjectId projectId = CfgUtil.getProjectId(project);
		return IdeaHelper.getCfgManager().getAllEnabledCrucibleServers(projectId).size() != 0;
	}

}