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

import com.atlassian.theplugin.commons.UiTaskAdapter;
import com.atlassian.theplugin.commons.UiTaskExecutor;
import com.atlassian.theplugin.commons.cfg.CfgManager;
import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.model.Review;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.CachingCommittedChangesProvider;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.RepositoryLocation;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.committed.CommittedChangesTreeBrowser;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class CrucibleCreatePostCommitReviewForm extends AbstractCrucibleCreatePostCommitReviewForm implements DataProvider {
	public static final String COMMITTED_CHANGES_BROWSER = "theplugin.crucible.committedchangesbrowser";
	public static final DataKey<CrucibleCreatePostCommitReviewForm> COMMITTED_CHANGES_BROWSER_KEY = DataKey
			.create(COMMITTED_CHANGES_BROWSER);

	private CommittedChangesTreeBrowser commitedChangesBrowser;
	private final UiTaskExecutor taskExecutor;
	private int revisionsNumber = 30;

	public CrucibleCreatePostCommitReviewForm(final Project project, final CrucibleServerFacade crucibleServerFacade,
			@NotNull final CfgManager cfgManager, @NotNull final UiTaskExecutor taskExecutor) {
		super(project, crucibleServerFacade, "", cfgManager);
		this.taskExecutor = taskExecutor;

		commitedChangesBrowser = new CommittedChangesTreeBrowser(project, Collections.<CommittedChangeList>emptyList());
		ActionManager manager = ActionManager.getInstance();
		ActionGroup group = (ActionGroup) manager.getAction("ThePlugin.CommittedChangesToolbar");
		ActionToolbar toolbar = manager.createActionToolbar("PostCommitReview", group, true);
		commitedChangesBrowser.addToolBar(toolbar.getComponent());
		setCustomComponent(commitedChangesBrowser);

		this.taskExecutor.execute(new ChangesRefreshTask("Fetching recent commits", getContentPane()));

		setTitle("Create Review");
		pack();
	}

	public void updateChanges() {
		CrucibleRevisionsNumber dialog = new CrucibleRevisionsNumber(revisionsNumber);
		dialog.show();
		if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
			revisionsNumber = dialog.getValue();
			this.taskExecutor.execute(new ChangesRefreshTask("Fetching recent commits", getContentPane()));
		}
	}

	@Override
	protected Review createReview(final CrucibleServerCfg server, final ReviewProvider reviewProvider)
			throws RemoteApiException, ServerPasswordNotProvidedException {
		final MyDataSink dataSink = new MyDataSink();
		//noinspection deprecation
		commitedChangesBrowser.calcData(DataKeys.CHANGE_LISTS, dataSink);
		final ChangeList[] changes = dataSink.getChanges();
		return createReviewImpl(server, reviewProvider, changes);
	}

	public Object getData(@NonNls final String dataId) {
		if (dataId.equals(COMMITTED_CHANGES_BROWSER)) {
			return this;
		}
		return null;
	}

	private class MyDataSink implements DataSink {
		public ChangeList[] getChanges() {
			return changes;
		}

		private ChangeList[] changes;

		public <T> void put(final DataKey<T> key, final T data) {
			changes = (ChangeList[]) data;
		}
	}

	private class ChangesRefreshTask extends UiTaskAdapter {
		List<CommittedChangeList> list;

		public ChangesRefreshTask(final String actionMame, final Component component) {
			super(actionMame, component);
		}

		public void run() throws Exception {
			commitedChangesBrowser.setEmptyText("Fetching recent commits...");
			IdeaVersionFacade.getInstance()
					.setCommitedChangesList(commitedChangesBrowser, Collections.<CommittedChangeList>emptyList(), false);
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
				list = committedChangesProvider.getCommittedChanges(changeBrowserSettings, repositoryLocation, revisionsNumber);
			}
		}

		@Override
		public void onSuccess() {
			commitedChangesBrowser.setEmptyText("No recent commits");
			IdeaVersionFacade.getInstance().setCommitedChangesList(commitedChangesBrowser, list, false);
		}
	}
}
