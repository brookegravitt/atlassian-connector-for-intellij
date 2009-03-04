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
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacade;
import com.atlassian.theplugin.commons.crucible.api.UploadItem;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitSession;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("deprecation")
public class CruciblePatchSubmitCommitSession implements CommitSession {
	private final Project project;
	protected final CrucibleServerFacade crucibleServerFacade;
	private final CfgManager cfgManager;

	public CruciblePatchSubmitCommitSession(Project project, CrucibleServerFacade crucibleServerFacade,
			final CfgManager cfgManager) {
		this.project = project;
		this.crucibleServerFacade = crucibleServerFacade;
		this.cfgManager = cfgManager;
	}

	@Nullable
	public JComponent getAdditionalConfigurationUI() {
		return null;
	}

	@Nullable
	public JComponent getAdditionalConfigurationUI(Collection<Change> changes, String commitMessage) {
		return null;
	}

	public boolean canExecute(Collection<Change> changes, String commitMessage) {
		return changes.size() > 0;
	}

	public void execute(Collection<Change> changes, String commitMessage) {
		Collection<UploadItem> uploadItems = new ArrayList<UploadItem>();
		for (Change change : changes) {
			try {
				FilePath path = change.getBeforeRevision().getFile();
				String fileUrl = VcsIdeaHelper.getRepositoryUrlForFile(project, path.getVirtualFile());

				try {
					URL url = new URL(fileUrl);
					fileUrl = url.getPath();
				} catch (MalformedURLException e) {
					String rootUrl = VcsIdeaHelper.getRepositoryRootUrlForFile(project, path.getVirtualFile());
					fileUrl = StringUtils.difference(rootUrl, fileUrl);
				}

				uploadItems.add(new UploadItem(fileUrl, change.getBeforeRevision().getContent(),
						change.getAfterRevision().getContent(), change.getBeforeRevision().getRevisionNumber().asString()));
			} catch (VcsException e) {
				throw new RuntimeException(e);
			}
		}

		ApplicationManager.getApplication().invokeAndWait(
				new CruciblePatchUploader(project, crucibleServerFacade, commitMessage,
						uploadItems.toArray(new UploadItem[uploadItems.size()]), cfgManager),
				ModalityState.defaultModalityState());

	}

	public void executionCanceled() {
	}
}
