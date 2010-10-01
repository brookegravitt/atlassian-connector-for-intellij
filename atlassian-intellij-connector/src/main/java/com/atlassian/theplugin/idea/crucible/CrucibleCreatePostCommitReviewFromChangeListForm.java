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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeList;
import org.jetbrains.annotations.NotNull;

public class CrucibleCreatePostCommitReviewFromChangeListForm extends AbstractCrucibleCreatePostCommitReviewForm {
	private final ChangeList[] changes;

	private static String getReviewTitle(final ChangeList[] changes) {
		if (changes != null && changes.length == 1) {
			return changes[0].getName();
		} else {
			return "";
		}

	}

	public CrucibleCreatePostCommitReviewFromChangeListForm(final Project project,
			final IntelliJCrucibleServerFacade crucibleServerFacade,
			final ChangeList[] changes, @NotNull final ProjectCfgManager projectCfgManager) {
		super(project, crucibleServerFacade, getReviewTitle(changes), projectCfgManager);
		this.changes = changes;
		setCustomComponent(null);
		pack();
	}

	@Override
	protected ReviewAdapter createReview(final ServerData server, final Review reviewBeingConstructed)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return createReviewImpl(server, reviewBeingConstructed, changes);
	}

}
