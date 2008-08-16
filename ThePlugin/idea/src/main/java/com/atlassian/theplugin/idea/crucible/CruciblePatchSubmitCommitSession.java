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

import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitSession;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

@SuppressWarnings("deprecation")
public class CruciblePatchSubmitCommitSession implements CommitSession {
	private final Project project;
	protected final CrucibleServerFacade crucibleServerFacade;

	public CruciblePatchSubmitCommitSession(Project project, CrucibleServerFacade crucibleServerFacade) {
		this.project = project;
		this.crucibleServerFacade = crucibleServerFacade;
	}

	@Nullable
	public JComponent getAdditionalConfigurationUI() {
		return null;
	}

	@Nullable
	public JComponent getAdditionalConfigurationUI(Collection<Change> changes, String commitMessage) {
		return null;
	}

	public boolean canExecute(Collection<Change> changes, String commitMessage) {
		return changes.size() > 0;
	}

	public void execute(Collection<Change> changes, String commitMessage) {
		PatchProducer patchProducer = new PatchProducer(project, changes);
		String patch = patchProducer.generateUnifiedDiff();
		ApplicationManager.getApplication().invokeAndWait(
				new CruciblePatchUploader(project, crucibleServerFacade, commitMessage, patch),
				ModalityState.defaultModalityState());

	}

	public void executionCanceled() {
	}
}
