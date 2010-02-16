package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.JFileChooser;
import java.io.File;

public class AddAttachmentAction extends JIRAAbstractAction {
	public void actionPerformed(AnActionEvent event) {
		IssueListToolWindowPanel panel = IdeaHelper.getIssueListToolWindowPanel(event);
		if (panel == null) {
			return;
		}

		JFileChooser fc = new JFileChooser();
		int ret = fc.showOpenDialog(null);

		if (ret != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File file = fc.getSelectedFile();
		panel.addAttachmentToSelectedIssue(file);
	}

	public void onUpdate(AnActionEvent event) {
	}

}
