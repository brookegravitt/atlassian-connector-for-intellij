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

import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.intellij.openapi.project.Project;

public class CruciblePatchUploader implements Runnable {
	private final String patch;
	private final CfgManager cfgManager;
	private final String commitMessage;
	private final CrucibleServerFacade crucibleServerFacade;
	private Project project;

	public CruciblePatchUploader(Project project, CrucibleServerFacade crucibleServerFacade, String commitMessage,
			String patch, final CfgManager cfgManager) {
		this.project = project;
		this.crucibleServerFacade = crucibleServerFacade;
		this.commitMessage = commitMessage;
		this.patch = patch;
		this.cfgManager = cfgManager;
	}

	public void run() {
		new CrucibleCreatePreCommitReviewForm(project, crucibleServerFacade, commitMessage, patch, cfgManager).show();
	}
}
