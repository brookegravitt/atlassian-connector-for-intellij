package com.atlassian.theplugin.idea.action;

import com.atlassian.theplugin.idea.PluginToolWindow;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Mar 26, 2008
 * Time: 3:19:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowHideMainPluginWindowAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		PluginToolWindow.showHidePluginWindow(e);		
	}
}
