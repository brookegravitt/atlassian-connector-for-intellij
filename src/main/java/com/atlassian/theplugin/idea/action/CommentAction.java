package com.atlassian.theplugin.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Aug 1, 2008
 * Time: 11:37:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class CommentAction extends AnAction {

	@Override
	public void update(final AnActionEvent e) {
		boolean enabled = true;
		Editor ed = e.getData(DataKeys.EDITOR);
		if (ed == null) {
			enabled = false;
		}
		String text = ed.getSelectionModel().getSelectedText();
		if (StringUtil.isEmptyOrSpaces(text)) {
			enabled = false;
		}
		e.getPresentation().setEnabled(enabled);
	}

	public void actionPerformed(final AnActionEvent e) {
		Editor ed = e.getData(DataKeys.EDITOR);
		if (ed == null) {
			return;
		}
		int start = ed.getSelectionModel().getSelectionStart();
		int end = ed.getSelectionModel().getSelectionStart();

	}
}
