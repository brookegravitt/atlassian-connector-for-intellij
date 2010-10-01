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

package com.atlassian.theplugin.idea.action.bamboo.changes;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.VcsIdeaHelper;
import com.atlassian.theplugin.idea.ui.tree.file.BambooFileNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class OpenRepoVersionAction extends AbstractBambooFileActions {

    public void actionPerformed(AnActionEvent e) {
		final Project project = IdeaHelper.getCurrentProject(e);
		final BambooFileNode bfn = getBambooFileNode(e);
		if (bfn != null && project != null && bfn.getPsiFile() != null) {
			final VirtualFile virtualFile = bfn.getPsiFile().getVirtualFile();
			final String url = VcsIdeaHelper.getRepositoryUrlForFile(project, virtualFile);
			if (url == null || virtualFile == null) {
				return;
			}

			VcsIdeaHelper.openFile(project, virtualFile, bfn.getRevision(), 0, 1, null);
		}

	}


}
