package com.atlassian.theplugin.idea.action.jira.editor;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ide.BrowserUtil;
import com.atlassian.theplugin.idea.jira.editor.ThePluginJIRAEditorComponent;
import com.atlassian.theplugin.jira.api.JIRAIssue;

public class EditorEditIssueAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
		ThePluginJIRAEditorComponent.JIRAFileEditor editor =
				ThePluginJIRAEditorComponent.getEditorByKey(event.getPlace());
		if (editor != null) {
			JIRAIssue issue = editor.getIssue();
			BrowserUtil.launchBrowser(issue.getServerUrl()
            	+ "/secure/EditIssue!default.jspa?key=" + issue.getKey());
		}
	}
}
