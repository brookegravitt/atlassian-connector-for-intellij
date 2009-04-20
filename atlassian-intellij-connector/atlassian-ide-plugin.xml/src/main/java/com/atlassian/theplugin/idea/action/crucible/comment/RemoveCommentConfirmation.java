package com.atlassian.theplugin.idea.action.crucible.comment;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.Icons;

/**
 * User: jgorycki
 * Date: Mar 10, 2009
 * Time: 1:41:09 PM
 */
public final class RemoveCommentConfirmation {
	private static final String ARE_YOU_SURE_YOU_WANT_REMOVE_YOUR_COMMENT = "Are you sure you want remove your comment?";
	private static final String CONFIRMATION_REQUIRED = "Confirmation required";

	private RemoveCommentConfirmation() { }
	
	public static boolean userAgreed(Project project) {
		int result = Messages.showYesNoDialog(project,
			ARE_YOU_SURE_YOU_WANT_REMOVE_YOUR_COMMENT, CONFIRMATION_REQUIRED, Icons.TASK_ICON);
		return result == DialogWrapper.OK_EXIT_CODE;
	}

//	public static boolean userAgreed2(Component parent) {
//		return JOptionPane.showConfirmDialog(parent, ARE_YOU_SURE_YOU_WANT_REMOVE_YOUR_COMMENT, CONFIRMATION_REQUIRED,
//				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, Icons.TASK_ICON) == JOptionPane.YES_OPTION;
//	}
}
