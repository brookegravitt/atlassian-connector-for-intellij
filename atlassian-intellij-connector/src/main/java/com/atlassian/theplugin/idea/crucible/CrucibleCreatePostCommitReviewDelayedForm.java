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

import com.atlassian.theplugin.commons.configuration.CrucibleConfigurationBean;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.Collection;
import java.util.List;

public class CrucibleCreatePostCommitReviewDelayedForm extends AbstractCrucibleCreatePostCommitReviewForm {
	private boolean doCreateReview = false;
	List<CommittedChangeList> list;
	private CrucibleConfigurationBean cruciblePluginConfig;
	private Collection<VirtualFile> virtualFiles;

	public CrucibleCreatePostCommitReviewDelayedForm(
			final Project project,
			final CrucibleServerFacade crucibleServerFacade,
			@NotNull final ProjectCfgManagerImpl projectCfgManager,
			CrucibleConfigurationBean cruciblePluginConfig,
			String title,
			Collection<VirtualFile> virtualFiles) {

		super(project, crucibleServerFacade, title, projectCfgManager);
		this.cruciblePluginConfig = cruciblePluginConfig;
		this.virtualFiles = virtualFiles;
		setCustomComponent(null);
		setReviewCreationTimeout(cruciblePluginConfig.getReviewCreationTimeout());
		pack();
	}

	@Override
	protected void doOKAction() {
		doCreateReview = true;
		doCancelAction();
	}

	@Override
	protected Review createReview(final ServerData server, final ReviewProvider reviewProvider)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		return createReviewImpl(server, reviewProvider, getChanges());
	}

	private ChangeList[] getChanges() {
		if (list.size() > 0) {
			for (CommittedChangeList committedChangeList : list) {
				if (isMyCommittedChangeList(committedChangeList)) {
					ChangeList[] chlist = new ChangeList[1];
					chlist[0] = committedChangeList;
					return chlist;
				}
			}
		}
		return null;
	}

	private boolean isMyCommittedChangeList(CommittedChangeList committedChangeList) {
		int verifiedChangesCnt = 0;
		for (VirtualFile virtualFile : virtualFiles) {
			for (Change change : committedChangeList.getChanges()) {
				if (change.affectsFile(new File(virtualFile.getPath()))) {
					++verifiedChangesCnt;
					break;
				}
			}
		}
		return verifiedChangesCnt == virtualFiles.size();
	}

	public void startReviewCreation() {
		if (doCreateReview) {
			doCreateReview = false;
			final Task.Backgroundable task = new ChangesRefreshTask();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ProgressManager.getInstance().run(task);
				}
			});
		}
	}

	private class ChangesRefreshTask extends Task.Backgroundable {
		private static final int REVISIONS_NUMBER = 30;

		public ChangesRefreshTask() {
			super(project, "Fetching recent commits");
		}

		public void run(@NotNull ProgressIndicator progressIndicator) {
			final VirtualFile baseDir = project.getBaseDir();
			if (baseDir == null) {
				throw new RuntimeException("Cannot determine base directory of the project");
			}
			ProjectLevelVcsManager mgr = ProjectLevelVcsManager.getInstance(project);
			AbstractVcs abstractVcs = mgr.getVcsFor(baseDir);
			if (abstractVcs != null) {
				@SuppressWarnings("unchecked")
				final CachingCommittedChangesProvider<CommittedChangeList, ChangeBrowserSettings> committedChangesProvider
						= abstractVcs.getCachingCommittedChangesProvider();
				if (committedChangesProvider == null) {
					throw new RuntimeException("Cannot determine VCS support for the project");
				}
				ChangeBrowserSettings changeBrowserSettings = new ChangeBrowserSettings();
				RepositoryLocation repositoryLocation = committedChangesProvider
						.getLocationFor(VcsUtil.getFilePath(baseDir.getPath()));
				try {
					list = committedChangesProvider.getCommittedChanges(
							changeBrowserSettings, repositoryLocation, REVISIONS_NUMBER);
				} catch (VcsException e) {
					LoggerImpl.getInstance().error(e);
				}
			}
		}

		@Override
		public void onSuccess() {
			runCreateReviewTask(true);
		}
	}
}