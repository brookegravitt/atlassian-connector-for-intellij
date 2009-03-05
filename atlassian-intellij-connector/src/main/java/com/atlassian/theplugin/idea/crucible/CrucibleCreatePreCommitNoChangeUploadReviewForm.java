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
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ui.MultipleChangeListBrowser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CrucibleCreatePreCommitNoChangeUploadReviewForm extends AbstractCrucibleCreatePreCommitReviewForm {
	private MultipleChangeListBrowser changesBrowser;

	public CrucibleCreatePreCommitNoChangeUploadReviewForm(final Project project,
			final CrucibleServerFacade crucibleServerFacade,
			@NotNull final CfgManager cfgManager) {
		super(project, crucibleServerFacade, "", cfgManager);

		ChangeListManager changeListManager = ChangeListManager.getInstance(project);
		changesBrowser = IdeaVersionFacade.getInstance().getChangesListBorwser(project, changeListManager);
		setCustomComponent(changesBrowser);

		setTitle("Create Review");
		pack();
	}

	@Override
	protected Review createReview(final CrucibleServerCfg server, final ReviewProvider reviewProvider)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		List<Change> changes = changesBrowser.getCurrentIncludedChanges();
		return createReviewImpl(server, reviewProvider, changes);
	}
}