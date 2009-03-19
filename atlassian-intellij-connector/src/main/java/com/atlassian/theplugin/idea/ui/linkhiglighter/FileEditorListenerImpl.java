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

import com.atlassian.theplugin.cfg.CfgUtil;
import com.atlassian.theplugin.commons.cfg.ConfigurationListenerAdapter;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

/**
 * User: pmaruszak
 */
public class FileEditorListenerImpl implements FileEditorManagerListener {
	private final Map<VirtualFile, JiraLinkHighlighter> linkHighlighters = new HashMap<VirtualFile, JiraLinkHighlighter>();
	private JiraEditorLinkParser jiraEditorLinkParser;
	private Project project;
	private boolean isRegistered = false;
	private LocalConfigurationListener localConfigurationListener;
	private JiraServerCfg lastJiraServer = null;


	public FileEditorListenerImpl(@NotNull Project project) {

		this.project = project;
		jiraEditorLinkParser = new JiraEditorLinkParser(project);
		localConfigurationListener = new LocalConfigurationListener();

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
			linkHighlighters.remove(oldFile);

		} else if (newFile != null && !linkHighlighters.containsKey(newFile)) {
			PsiFile psiFile = PsiManager.getInstance(project).findFile(newFile);
			if (psiFile != null) {
				Editor editor = editorManager.getSelectedTextEditor();
				if (editor != null) {
					addHighlighter(newFile, psiFile, editor);
				}
			}
		}
	}

	public void projectClosed() {
		IdeaHelper.getCfgManager()
				.removeProjectConfigurationListener(CfgUtil.getProjectId(project), localConfigurationListener);
		deactivate();
	}

	public void projectOpened() {
		IdeaHelper.getCfgManager()
				.addProjectConfigurationListener(CfgUtil.getProjectId(project), localConfigurationListener);
		activate();
		if (IdeaHelper.getCfgManager()
				.getProjectConfiguration(CfgUtil.getProjectId(project)).getDefaultJiraServer() != null) {
			Task.Backgroundable task = new ScanningJiraLinksTask(project, FileEditorListenerImpl.this);
			ProgressManager.getInstance().run(task);
		}
	}

	public void deactivate() {
		FileEditorManager.getInstance(project).removeFileEditorManagerListener(this);
		isRegistered = false;
	}

	public void activate() {

		if (!isRegistered) {
			FileEditorManager.getInstance(project).addFileEditorManagerListener(FileEditorListenerImpl.this);
			isRegistered = true;
		}

	}

	private void addHighlighter(final VirtualFile newFile, final PsiFile psiFile, final Editor editor) {
		JiraLinkHighlighter highlighter = new JiraLinkHighlighter(project, newFile, psiFile, editor, jiraEditorLinkParser);
		highlighter.startListeninig();
		linkHighlighters.put(newFile, highlighter);
		highlighter.reparseAll();
		highlighter.checkComments();
	}


	private void removeHighlighter(final VirtualFile oldFile) {
		JiraLinkHighlighter hl = linkHighlighters.get(oldFile);
		if (hl != null) {
			hl.stopListening();
			hl.removeAllRanges();
		}

	}

	public void scanOpenEditors() {
		final FileEditorManager editorManager = FileEditorManager.getInstance(project);
		for (VirtualFile openFile : editorManager.getSelectedFiles()) {
			if (!linkHighlighters.containsKey(openFile)) {
				PsiFile psiFile = PsiManager.getInstance(project).findFile(openFile);
				if (psiFile != null) {
					Editor editor = editorManager.getSelectedTextEditor();
					if (editor != null) {
						addHighlighter(openFile, psiFile, editor);
					}
				}

			} else {
				linkHighlighters.get(openFile).reparseAll();
				linkHighlighters.get(openFile).checkComments();
			}
		}

	}

	public void removeAllLinkHighlighers() {
		for (VirtualFile vf : linkHighlighters.keySet()) {
			removeHighlighter(vf);
		}
		linkHighlighters.clear();

	}

	class LocalConfigurationListener extends ConfigurationListenerAdapter {
		public void configurationUpdated(final ProjectConfiguration aProjectConfiguration) {

			final JiraServerCfg currentJiraServer = aProjectConfiguration.getDefaultJiraServer();
			if (currentJiraServer == null) {
				FileEditorListenerImpl.this.deactivate();
				removeAllLinkHighlighers();


			} else if (!currentJiraServer.equals(lastJiraServer)) {
				Task.Backgroundable task = new ScanningJiraLinksTask(project, FileEditorListenerImpl.this);
				FileEditorListenerImpl.this.activate();
				ProgressManager.getInstance().run(task);
			}

			lastJiraServer = currentJiraServer;

		}
	}


	private class ScanningJiraLinksTask extends Task.Backgroundable {
		private final FileEditorListenerImpl fileEditor;

		public ScanningJiraLinksTask(@org.jetbrains.annotations.Nullable Project project,
				final FileEditorListenerImpl fileEditor) {
			super(project, "Scanning files for JIRA issues links", false);
			this.fileEditor = fileEditor;
		}

		public void run(final ProgressIndicator progressIndicator) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fileEditor.scanOpenEditors();
				}
			});
		}

	}
}
