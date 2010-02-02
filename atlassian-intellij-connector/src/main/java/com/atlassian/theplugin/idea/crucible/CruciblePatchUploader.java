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

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;

import java.util.Collection;

public class CruciblePatchUploader implements Runnable {
	private final ProjectCfgManager cfgManager;
	private final CrucibleServerFacade crucibleServerFacade;
	private Project project;
	private Collection<Change> changes;

	public CruciblePatchUploader(Project project, CrucibleServerFacade crucibleServerFacade,
			final ProjectCfgManager projectCfgManager) {
		this.project = project;
		this.crucibleServerFacade = crucibleServerFacade;
		this.cfgManager = projectCfgManager;
	}

	public CruciblePatchUploader(Project project, CrucibleServerFacade crucibleServerFacade,
			Collection<Change> changes, final ProjectCfgManager projectCfgManager) {
		this.project = project;
		this.crucibleServerFacade = crucibleServerFacade;
		this.changes = changes;
		this.cfgManager = projectCfgManager;
	}

	public void run() {
		new CrucibleCreatePreCommitUploadReviewForm(project, crucibleServerFacade, changes, cfgManager)
				.show();
	}
}
