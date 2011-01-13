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
import com.atlassian.connector.intellij.crucible.IntelliJCrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.PatchAnchorData;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public abstract class AbstractCrucibleCreatePreCommitReviewForm extends CrucibleReviewCreateForm {
	public AbstractCrucibleCreatePreCommitReviewForm(Project project, IntelliJCrucibleServerFacade crucibleServerFacade,
			String commitMessage,
			@NotNull final ProjectCfgManager projectCfgManager) {
		super(project, crucibleServerFacade, commitMessage, projectCfgManager, "Create Pre-Commit Review");
	}

	@Override
	protected boolean isValid(final Review review) {
		return true;
	}

	@Override
	protected boolean shouldAutoSelectRepo(final CrucibleServerData crucibleServerData) {
		return false;
	}

	@Override
	protected boolean shouldShowRepo() {
		return false;
	}

	protected ReviewAdapter createReviewImpl(final ServerData server, final Review reviewBeingConstructed,
			final List<Change> changes)
			throws RemoteApiException, ServerPasswordNotProvidedException, VcsException, IOException {

		PatchAnchorData anchorData = isAnchorDataAvailable() ? getPatchAnchorData() : null;
		return crucibleServerFacade
				.createReviewFromPatch(server, reviewBeingConstructed,
						CrucibleHelper.getPatchFromChanges(project, changes), anchorData);
	}


}