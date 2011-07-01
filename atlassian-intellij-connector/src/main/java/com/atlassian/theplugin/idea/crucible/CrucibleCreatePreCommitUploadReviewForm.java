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
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.exception.PatchCreateErrorException;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ui.MultipleChangeListBrowser;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class CrucibleCreatePreCommitUploadReviewForm extends AbstractCrucibleCreatePreCommitReviewForm {
	public static final String LOCAL_CHANGES_BROWSER = "theplugin.crucible.localchangesbrowser";
	public static final DataKey<CrucibleCreatePostCommitReviewForm> COMMITTED_CHANGES_BROWSER_KEY = DataKey
			.create(LOCAL_CHANGES_BROWSER);

	private final MultipleChangeListBrowser changesBrowser;

	public CrucibleCreatePreCommitUploadReviewForm(final Project project, final IntelliJCrucibleServerFacade crucibleServerFacade,
			Collection<Change> changes,
			@NotNull final ProjectCfgManager projectCfgManager) {
		super(project, crucibleServerFacade, "", projectCfgManager);

		ChangeListManager changeListManager = ChangeListManager.getInstance(project);
		changesBrowser = IdeaVersionFacade.getInstance().getChangesListBrowser(project, changeListManager, changes);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setCustomComponent(changesBrowser);

				setTitle("Create Pre-Commit Review");
				pack();
			}
		});

	}

	@Override
	protected boolean isPatchForm() {
		return true;
	}

	@Override
	protected ReviewAdapter createReview(final ServerData server, final Review reviewProvider)
			throws RemoteApiException, ServerPasswordNotProvidedException, VcsException, IOException,
			PatchCreateErrorException {
		List<Change> changes = changesBrowser.getCurrentIncludedChanges();
		return createReviewImpl(server, reviewProvider, changes);
	}
}