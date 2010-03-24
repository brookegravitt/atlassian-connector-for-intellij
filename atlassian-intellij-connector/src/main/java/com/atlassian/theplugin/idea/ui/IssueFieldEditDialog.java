package com.atlassian.theplugin.idea.ui;

import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * User: pstefaniak
 * Date: Mar 22, 2010
 */
public class IssueFieldEditDialog extends DialogWrapper {
	private JComboBox comboBox = new JComboBox();
	private JPanel rootPanel = new JPanel(new BorderLayout());
	private final JiraIssueAdapter issue;
	private final Project project;
	private final ResultHandler handler;

	public IssueFieldEditDialog(Project project, JiraIssueAdapter issue, String item, ResultHandler handler) {
		super(project, false);
		this.issue = issue;
		this.project = project;
		this.handler = handler;

		setOKButtonText("Change");
		setModal(true);
		comboBox.addItem(item);
		comboBox.setEditable(true);
		rootPanel.add(new JLabel("New value:"), BorderLayout.WEST);
		rootPanel.add(comboBox, BorderLayout.CENTER);
		rootPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		init();
		setTitle("Change Issue Field");
		show();
	}

	@Override
	protected JComponent createCenterPanel() {
		return rootPanel;
	}

	@Override
	protected void doOKAction() {
		ProgressManager.getInstance().run(new Task.Backgroundable(project,
				"Updating issue " + issue.getKey(), false) {
			@Override
			public void run(@NotNull ProgressIndicator progressIndicator) {
				if (handler != null) {
					String selectedType = comboBox.getSelectedItem().toString();
					handler.handleOK(selectedType);
				}
			}
		});
		super.doOKAction();
	}

	public static abstract class ResultHandler {
		abstract public void handleOK(String newFieldValue);
	}
}