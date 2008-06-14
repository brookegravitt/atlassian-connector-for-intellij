package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.jira.editor.ThePluginJIRAEditorComponent;
import com.atlassian.theplugin.idea.jira.editor.vfs.MemoryVirtualFile;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.vfs.VirtualFile;

public class OpenIssueAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		JIRAIssue issue = IdeaHelper.getJIRAToolWindowPanel(event).getCurrentIssue();
        String fileName = issue.getKey();// + "." + ThePluginJIRAEditorComponent.SUPPORTED_EXTENSION;
        FileEditorManager manager =
                FileEditorManager.getInstance(DataKeys.PROJECT.getData(event.getDataContext()));
        VirtualFile[] files = manager.getOpenFiles();
        VirtualFile vf = null;
        for (VirtualFile f : files) {
            if (f.getName().equals(fileName)) {
                vf = f;
                break;
            }

        }
        if (vf == null) {
            vf = new MemoryVirtualFile(fileName);
		    manager.openFile(vf, false);
        } else {
            // todo: bring editor to front
        }
    }
}