package com.atlassian.theplugin.idea.action.issues;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssueListToolWindowPanel;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.JFileChooser;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
		byte[] contents = getContents(file);
		panel.addAttachmentToSelectedIssue(file.getName(), contents);
	}

	public void onUpdate(AnActionEvent event) {
	}

	private static byte[] getContents(final File file) {
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			byte[] buf = new byte[(int) file.length()];
			bis.read(buf);
			bis.close();
			return buf;
		} catch (IOException ex) {
			return null;
		}
	}
}
