package com.atlassian.theplugin.idea.action.crucible;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 19, 2008
 * Time: 9:09:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class BackToRevisionCommentsAction extends AnAction {
	private AnActionEvent event;

	public void actionPerformed(AnActionEvent event) {
		this.event = event;
	}
}
