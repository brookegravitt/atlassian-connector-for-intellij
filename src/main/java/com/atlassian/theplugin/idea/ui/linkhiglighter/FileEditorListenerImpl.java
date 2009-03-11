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
package com.atlassian.theplugin.idea.ui.linkhiglighter;

import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * User: pmaruszak
 */
public class FileEditorListenerImpl implements FileEditorManagerListener {
	private final Map<VirtualFile, JiraLinkHighlighter> highlighters = new HashMap<VirtualFile, JiraLinkHighlighter>();
	JiraEditorLinkParser jiraEditorLinkParser;
	private Project project;


	public FileEditorListenerImpl(@NotNull Project project) {

		this.project = project;
		jiraEditorLinkParser = new JiraEditorLinkParser(project);
	}

	public void fileOpened(final FileEditorManager fileEditorManager, final VirtualFile virtualFile) {
		PluginUtil.getLogger()
				.debug(" file opened: " + virtualFile.getPath() + ", source: " + fileEditorManager.getSelectedTextEditor());
	}

	public void fileClosed(final FileEditorManager fileEditorManager, final VirtualFile virtualFile) {
		PluginUtil.getLogger()
				.debug(" file closed: " + virtualFile.getPath() + ", source: " + fileEditorManager.getSelectedTextEditor());
	}

	public void selectionChanged(final FileEditorManagerEvent event) {
		final FileEditorManager editorManager = event.getManager();
		if (project != editorManager.getProject()) {
			assert false : this;
			return;
		}
		VirtualFile newFile = event.getNewFile();

		VirtualFile oldFile = event.getOldFile();

		if (oldFile != null && newFile == null) {
			removeHighlighter(oldFile);

		} else if (newFile != null && !highlighters.containsKey(newFile)) {
			PsiFile psiFile = PsiManager.getInstance(project).findFile(newFile);
			if (psiFile != null) {
				Editor editor = editorManager.getSelectedTextEditor();
				if (editor != null) {
					addHighlighter(newFile, psiFile, editor, jiraEditorLinkParser);
				}
			}
		}
		checkTabLimitReached(editorManager, newFile);
	}

	private void checkTabLimitReached(final FileEditorManager editorManager, final VirtualFile newFile) {
		///@todo: implement
	}

	private void addHighlighter(final VirtualFile newFile, final PsiFile psiFile, final Editor editor,
			JiraEditorLinkParser jiraEditorLinkParser) {
		JiraLinkHighlighter highlighter = new JiraLinkHighlighter(newFile, psiFile, editor, jiraEditorLinkParser);
		highlighter.startListeninig();
		highlighters.put(newFile, highlighter);
		highlighter.reparseAll();
		highlighter.checkComments();
	}


	private void removeHighlighter(final VirtualFile oldFile) {
		JiraLinkHighlighter hl = highlighters.remove(oldFile);
		if (hl != null) {
			hl.stopListening();
		}
	}

	public void scanOpenEditors() {
		for (JiraLinkHighlighter highlighter : highlighters.values()) {
			highlighter.reparseAll();
			highlighter.checkComments();
		}
	}

	public void projectClosed() {
		if (project != null) {
			for (VirtualFile vf : highlighters.keySet()) {
				removeHighlighter(vf);
			}
		}
		project = null;
	}
}
