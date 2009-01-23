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
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class CrucibleCreatePostCommitReviewForm extends CrucibleReviewCreateForm {

	private CommittedChangesTreeBrowser commitedChangesBrowser;

//	public CrucibleReviewCreateForm(final Project project, final CrucibleServerFacade crucibleServerFacade, @NotNull final CfgManager cfgManager) {
//		this(project, crucibleServerFacade, "", cfgManager);
//
//		repositoryPanel.setLayout(new BorderLayout());
//		repositoryPanel.add(new JLabel("Fetching recent commits...", JLabel.CENTER));
//
//
//		showPatchPanel(false);
//		setTitle("Create Review");
//		pack();
//
//
//	}


	

	public CrucibleCreatePostCommitReviewForm(final Project project, final CrucibleServerFacade crucibleServerFacade,
			@NotNull final CfgManager cfgManager, @NotNull final UiTaskExecutor taskExecutor) {
		super(project, crucibleServerFacade, "", cfgManager, "Create Post-Commit Review");

		setCustomComponent(new JLabel("Fetching recent commits...", JLabel.CENTER));

		taskExecutor.execute(new UiTaskAdapter("Fetching recent commits", getContentPane()) {
			List<CommittedChangeList> list;

			public void run() throws Exception {
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
					list = committedChangesProvider.getCommittedChanges(changeBrowserSettings, repositoryLocation, 30);
				}
			}

			@Override
			public void onSuccess() {
				commitedChangesBrowser = new CommittedChangesTreeBrowser(project, list);
				ActionManager manager = ActionManager.getInstance();
				ActionGroup group = (ActionGroup) manager.getAction("CommittedChangesToolbar");
				ActionToolbar toolbar = manager.createActionToolbar("PostCommitReview", group, true);
				commitedChangesBrowser.addToolBar(toolbar.getComponent());
				setCustomComponent(commitedChangesBrowser);
			}
		});

		setTitle("Create Review");
		pack();
	}

	@Override
	protected Review createReview(final CrucibleServerCfg server, final ReviewProvider reviewProvider)
			throws RemoteApiException, ServerPasswordNotProvidedException {

		if (reviewProvider.getRepoName() == null) {
			Messages.showErrorDialog(project, "Repository not selected. Unable to create review.\n", "Repository required");
			return null;
		}
		final MyDataSink dataSink = new MyDataSink();
		//noinspection deprecation
		commitedChangesBrowser.calcData(DataKeys.CHANGE_LISTS, dataSink);
		final ChangeList[] changes = dataSink.getChanges();
		List<String> revisions = new ArrayList<String>();
		if (changes != null) {
			for (ChangeList change : changes) {
				if (change instanceof CommittedChangeList) {
					CommittedChangeList committedChangeList = (CommittedChangeList) change;
					revisions.add(Long.toString(committedChangeList.getNumber()));
				}
			}
		}
		return crucibleServerFacade.createReviewFromRevision(server, reviewProvider, revisions);

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

	@Override
	protected boolean isValid(final ReviewProvider reviewProvider) {
		return (reviewProvider.getRepoName() != null);
	}

	@Override
	protected boolean shouldAutoSelectRepo(final CrucibleReviewCreateForm.CrucibleServerData crucibleServerData) {
		return crucibleServerData.getRepositories().size() == 1;
	}
}
