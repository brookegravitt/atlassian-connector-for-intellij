package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Apr 3, 2008
 * Time: 10:56:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class JIRAShowIssuesFilterAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		IdeaHelper.getCurrentJIRAToolWindowPanel().showJIRAIssueFilter();
	}
}
