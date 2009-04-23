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
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.AbstractCfgManager;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public abstract class AbstractCrucibleCreatePostCommitReviewForm extends CrucibleReviewCreateForm {
	public AbstractCrucibleCreatePostCommitReviewForm(Project project, CrucibleServerFacade crucibleServerFacade,
			String commitMessage,
			@NotNull final CfgManager cfgManager) {
		super(project, crucibleServerFacade, commitMessage, cfgManager, "Create Post-Commit Review");
	}

	@Override
	protected boolean isValid(final ReviewProvider reviewProvider) {
		return (reviewProvider.getRepoName() != null);
	}

	@Override
	protected boolean shouldAutoSelectRepo(final CrucibleServerData crucibleServerData) {
		return crucibleServerData.getRepositories().size() == 1;
	}

	protected Review createReviewImpl(final ServerData server, final ReviewProvider reviewProvider,
			final ChangeList[] changes) throws RemoteApiException, ServerPasswordNotProvidedException {
		if (reviewProvider.getRepoName() == null) {
			Messages.showErrorDialog(project, "Repository not selected. Unable to create review.\n", "Repository required");
			return null;
		}
		java.util.List<String> revisions = new ArrayList<String>();
		if (changes != null) {
			for (ChangeList change : changes) {
				if (change instanceof CommittedChangeList) {
					CommittedChangeList committedChangeList = (CommittedChangeList) change;
					revisions.add(Long.toString(committedChangeList.getNumber()));
				}
			}
		}

		if (revisions.isEmpty()) {
			return crucibleServerFacade.createReview(server, reviewProvider);
		} else {
			return crucibleServerFacade.createReviewFromRevision(server, reviewProvider, revisions);
		}

	}

}
