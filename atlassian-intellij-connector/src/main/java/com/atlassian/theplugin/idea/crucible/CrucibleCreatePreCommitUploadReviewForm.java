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
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ui.MultipleChangeListBrowser;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class CrucibleCreatePreCommitUploadReviewForm extends AbstractCrucibleCreatePreCommitReviewForm {
	public static final String LOCAL_CHANGES_BROWSER = "theplugin.crucible.localchangesbrowser";
	public static final DataKey<CrucibleCreatePostCommitReviewForm> COMMITTED_CHANGES_BROWSER_KEY = DataKey
			.create(LOCAL_CHANGES_BROWSER);

	private MultipleChangeListBrowser changesBrowser;

	public CrucibleCreatePreCommitUploadReviewForm(final Project project, final CrucibleServerFacade crucibleServerFacade,
			Collection<Change> changes,
			@NotNull final ProjectCfgManagerImpl projectCfgManager) {
		super(project, crucibleServerFacade, "", projectCfgManager);

		ChangeListManager changeListManager = ChangeListManager.getInstance(project);
		changesBrowser = IdeaVersionFacade.getInstance().getChangesListBrowser(project, changeListManager, changes);

		setCustomComponent(changesBrowser);

		setTitle("Create Review");
		pack();
	}

	@Override
	protected Review createReview(final ServerData server, final ReviewProvider reviewProvider)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		List<Change> changes = changesBrowser.getCurrentIncludedChanges();
		return createReviewImpl(server, reviewProvider, changes);
	}
}