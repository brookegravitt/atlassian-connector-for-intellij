package com.atlassian.theplugin.idea.action.tree.file;

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.CrucibleReviewWindow;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Aug 5, 2008
 * Time: 4:11:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToggleFilterFilesAction extends AnAction {
	static final Icon FILTER_ON_ICON = IconLoader.getIcon("/actions/unselectall.png");
	static final Icon FILTER_OFF_ICON = IconLoader.getIcon("/actions/selectall.png");
	static final String TEXT_FILTER_ON = "Show Files with comments only";
	static final String TEXT_FILTER_OFF = "Show all files";

	public ToggleFilterFilesAction() {
		getTemplatePresentation().setIcon(FILTER_ON_ICON);
		getTemplatePresentation().setText(TEXT_FILTER_ON);
	}

	public void actionPerformed(final AnActionEvent e) {
		CrucibleReviewWindow window = (CrucibleReviewWindow) e.getDataContext().getData(Constants.CRUCIBLE_BOTTOM_WINDOW);
		if (window != null) {
			window.switchFilter();			
			switchIcons(e.getPresentation());
		}
	}

	private void switchIcons(final Presentation presentation) {
		if (presentation.getIcon().equals(FILTER_ON_ICON)) {
			presentation.setIcon(FILTER_OFF_ICON);
			presentation.setText(TEXT_FILTER_OFF);
		} else {
			presentation.setIcon(FILTER_ON_ICON);
			presentation.setText(TEXT_FILTER_ON);
		}
	}

	public void update(final AnActionEvent e) {
		boolean enabled = true;
		CrucibleReviewWindow window = (CrucibleReviewWindow) e.getDataContext().getData(Constants.CRUCIBLE_BOTTOM_WINDOW);
		if (window == null) {
			enabled = false;
		}
		e.getPresentation().setEnabled(enabled);
	}
}
