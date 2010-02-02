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
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.changes.CommitSession;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CruciblePatchSubmitExecutor implements CommitExecutor {
	private final Project project;
	private final CrucibleServerFacade crucibleServerFacade;
	private final ProjectCfgManager projectCfgManager;

	public CruciblePatchSubmitExecutor(Project project, CrucibleServerFacade crucibleServerFacade,
			final ProjectCfgManager projectCfgManager) {
		this.project = project;
		this.crucibleServerFacade = crucibleServerFacade;
		this.projectCfgManager = projectCfgManager;
	}

	@NotNull
	public Icon getActionIcon() {
		//TODO: implement method getActionIcon
		throw new UnsupportedOperationException("method getActionIcon not implemented");
	}

	@Nls
	public String getActionText() {
		return "Crucible Pre-commit Review...";
	}

	@Nls
	public String getActionDescription() {
		return "Creates a patch from the files that would be commited and sends it for review to the Crucible server.";
	}

	@NotNull
	public CommitSession createCommitSession() {
		return new CruciblePatchSubmitCommitSession(project, crucibleServerFacade, projectCfgManager);
	}
}
