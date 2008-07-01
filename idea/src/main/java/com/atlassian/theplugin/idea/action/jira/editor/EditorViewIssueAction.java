package com.atlassian.theplugin.idea.action.jira.editor;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ide.BrowserUtil;
import com.atlassian.theplugin.idea.jira.editor.ThePluginJIRAEditorComponent;

public class EditorViewIssueAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		ThePluginJIRAEditorComponent.JIRAFileEditor editor =
				ThePluginJIRAEditorComponent.getEditorByKey(event.getPlace());
		if (editor != null) {
			BrowserUtil.launchBrowser(editor.getIssue().getIssueUrl());
		}
	}
}
