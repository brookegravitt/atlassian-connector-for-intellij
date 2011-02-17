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

package com.atlassian.theplugin.idea;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.CommittedChangesProvider;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.RepositoryLocation;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.Nullable;

public final class VcsIdeaHelper {

	private VcsIdeaHelper() {
	}


	public static String getRepositoryRootUrlForFile(Project project, VirtualFile vFile) {
		ProjectLevelVcsManager plm = ProjectLevelVcsManager.getInstance(project);
		if (plm == null) {
			return null;
		}
		VirtualFile vcsRoot = plm.getVcsRootFor(vFile);
		return getRepositoryUrlForFile(project, vcsRoot);
	}

	@Nullable
	public static String getRepositoryUrlForFile(Project project, VirtualFile vFile) {
        // PL-1619
        if (vFile == null) {
            return null;
        }

		ProjectLevelVcsManager plm = ProjectLevelVcsManager.getInstance(project);
		if (plm == null) {
			return null;
		}

		AbstractVcs vcs = plm.getVcsFor(vFile);
		if (vcs == null) {
			return null;
		}
		CommittedChangesProvider<?, ?> provider = vcs.getCommittedChangesProvider();
		if (provider == null) {
			return null;
		}
		RepositoryLocation repositoryLocation = provider.getLocationFor(VcsUtil.getFilePath(vFile.getPath()));
		if (repositoryLocation == null) {
			return null;
		}
		return repositoryLocation.toPresentableString();
	}
}
