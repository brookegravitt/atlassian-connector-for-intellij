package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.jira.editor.ThePluginJIRAEditorComponent;
import com.atlassian.theplugin.idea.jira.editor.vfs.MemoryVirtualFile;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;

public class OpenIssueAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		JIRAIssue issue = IdeaHelper.getJIRAToolWindowPanel(event).getCurrentIssue();
		VirtualFile vf = new MemoryVirtualFile(issue.getKey() + "." + ThePluginJIRAEditorComponent.SUPPORTED_EXTENSION);

		FileEditorManager fileEditorManager =
				FileEditorManager.getInstance(DataKeys.PROJECT.getData(event.getDataContext()));
		fileEditorManager.openFile(vf, false);
	}
}