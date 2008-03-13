package com.atlassian.theplugin.idea.action;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;

import javax.swing.*;

public class ReviewScope extends ComboBoxAction {
	protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
		return (DefaultActionGroup) ActionManager.getInstance().getAction("ThePlugin.ReviewScopeCombo");
	}
}
