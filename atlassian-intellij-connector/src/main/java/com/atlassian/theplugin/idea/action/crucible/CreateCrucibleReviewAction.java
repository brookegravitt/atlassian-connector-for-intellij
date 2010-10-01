package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.theplugin.idea.action.reviews.AbstractCrucibleToolbarAction;
import com.intellij.openapi.actionSystem.*;

import javax.swing.*;

/**
 * User: jgorycki
 * Date: Mar 24, 2009
 * Time: 3:49:59 PM
 */
public class CreateCrucibleReviewAction extends AbstractCrucibleToolbarAction {
	public void actionPerformed(AnActionEvent e) {
		final DefaultActionGroup actionGroup = new DefaultActionGroup();
		final ActionGroup configActionGroup = (ActionGroup) ActionManager
				.getInstance().getAction("ThePlugin.Crucible.CreateCrucibleReviewGroup");
		actionGroup.addAll(configActionGroup);

		final ActionPopupMenu popup = ActionManager.getInstance().createActionPopupMenu(e.getPlace(), actionGroup);

		final JPopupMenu jPopupMenu = popup.getComponent();

		jPopupMenu.show(e.getInputEvent().getComponent(),
				e.getInputEvent().getComponent().getWidth(), e.getInputEvent().getComponent().getY());
	}
}
