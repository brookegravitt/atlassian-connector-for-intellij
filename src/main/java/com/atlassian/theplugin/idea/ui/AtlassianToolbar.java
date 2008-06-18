package com.atlassian.theplugin.idea.ui;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionToolbar;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jun 18, 2008
 * Time: 7:35:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class AtlassianToolbar extends JPanel {
	public AtlassianToolbar(String toolbarPlace, String toolbarName) {
		super(new BorderLayout());
		if (toolbarName != null && toolbarPlace.length() > 0 && toolbarName.length() > 0) {
			ActionManager aManager = ActionManager.getInstance();
			ActionGroup serverToolBar = (ActionGroup) aManager.getAction(toolbarName);
			ActionToolbar actionToolbar = aManager.createActionToolbar(
					toolbarPlace, serverToolBar, true);

			add(actionToolbar.getComponent(), BorderLayout.NORTH);
		}
	}
}
