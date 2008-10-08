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

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.idea.action.fisheye.AbstractCrucibleAction;
import com.atlassian.theplugin.idea.action.fisheye.ChangeListUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;


public abstract class Crucible16RepositoryAction extends AbstractCrucibleAction {
	@Override
	public void update(AnActionEvent event) {
		event.getPresentation().setVisible(false);
		CrucibleServerCfg crucibleServerCfg = getCrucibleServerCfg(event);
		if (crucibleServerCfg != null) {
				if (crucibleServerCfg.getProjectName() != null
						&& crucibleServerCfg.getRepositoryName() != null) {
					event.getPresentation().setVisible(true);
				}
		}
		
		if (event.getPresentation().isVisible()) {
			event.getPresentation().setEnabled(ChangeListUtil.getRevision(event) != null);
		}
	}
}