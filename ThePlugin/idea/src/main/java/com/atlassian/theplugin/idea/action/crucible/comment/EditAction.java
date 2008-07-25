package com.atlassian.theplugin.idea.action.crucible.comment;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 23, 2008
 * Time: 3:49:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		Component component = DataKeys.CONTEXT_COMPONENT.getData(e.getDataContext());
		
	}
}
