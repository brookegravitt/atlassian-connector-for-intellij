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
import com.intellij.openapi.diff.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public class ShowDiffAction extends AbstractBambooFileActions {

	public void showRevisionDiff(final Project project, final BambooFileNode bambooFileNode) {
		final PsiFile psiFile = bambooFileNode.getPsiFile();
		if (psiFile == null) {
			Messages.showErrorDialog(project, "Cannot find corresponding file in the project.", "Problem");
			return;
		}
		final VirtualFile virtualFile = psiFile.getVirtualFile();
		if (virtualFile == null) {
			Messages.showErrorDialog(project, "PsiFile has not corresponding VirtualFile.", "Problem");
			return;
		}
		final VcsRevisionNumber currentRevisionNumber = VcsIdeaHelper.getVcsRevisionNumber(project, virtualFile);
		if (currentRevisionNumber == null) {
			Messages.showErrorDialog(project, "Cannot determine current version of file ["
					+ psiFile.getName() + "] in the project.", "Problem");
			return;
		}

		VcsIdeaHelper.openFile(project, virtualFile, bambooFileNode.getRevision(), 1, 1,
				new VcsIdeaHelper.OpenFileDescriptorAction() {

					public boolean shouldNavigate() {
						return false;
					}

					public void run(OpenFileDescriptor ofd) {

						final Document displayDocument = new FileContent(project, ofd.getFile()).getDocument();
						final Document referenceDocument = new FileContent(project, virtualFile).getDocument();

						DiffRequest request = new DiffRequest(project) {

							@Override
							public DiffContent[] getContents() {
								return (new DiffContent[]{
										new DocumentContent(project, displayDocument),
										new DocumentContent(project, referenceDocument),
								});
							}

							@Override
							public String[] getContentTitles() {
								return (new String[]{
										VcsBundle.message("diff.content.title.repository.version",
												bambooFileNode.getRevision()), "Your version"
								});
							}

							@Override
							public String getWindowTitle() {
								return "Diff between revisions " + bambooFileNode.getRevision() + " and "
										+ currentRevisionNumber.asString() + " of file " + psiFile.getName();
							}
						};
						DiffManager.getInstance().getDiffTool().show(request);
					}
				});
	}


	public void actionPerformed(AnActionEvent event) {
		showRevisionDiff(IdeaHelper.getCurrentProject(event), getBambooFileNode(event));

	}

}
