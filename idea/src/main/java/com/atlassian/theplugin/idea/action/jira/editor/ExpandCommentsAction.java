package com.atlassian.theplugin.idea.action.jira.editor;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.idea.jira.editor.ThePluginJIRAEditorComponent;

public class ExpandCommentsAction extends AnAction {
	public void actionPerformed(AnActionEvent event) {
        ThePluginJIRAEditorComponent.JIRAFileEditor editor =
                ThePluginJIRAEditorComponent.getEditorByKey(event.getPlace());
        if (editor != null) {
            editor.setCommentsExpanded(true);
        }
	}
}
