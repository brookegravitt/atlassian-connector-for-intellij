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

import com.atlassian.connector.intellij.crucible.CrucibleServerFacade;
import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class AbstractCrucibleCreatePreCommitReviewForm extends CrucibleReviewCreateForm {
	public AbstractCrucibleCreatePreCommitReviewForm(Project project, CrucibleServerFacade crucibleServerFacade,
			String commitMessage,
			@NotNull final ProjectCfgManagerImpl projectCfgManager) {
		super(project, crucibleServerFacade, commitMessage, projectCfgManager, "Create Pre-Commit Review");
	}

	@Override
	protected boolean isValid(final ReviewProvider reviewProvider) {
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

	protected ReviewAdapter createReviewImpl(final ServerData server, final ReviewProvider reviewProvider,
			final Collection<Change> changes) throws RemoteApiException, ServerPasswordNotProvidedException {
		Collection<UploadItem> uploadItems = CrucibleHelper.getUploadItemsFromChanges(project, changes);
		return crucibleServerFacade
				.createReviewFromUpload(server, reviewProvider, uploadItems);
	}


}