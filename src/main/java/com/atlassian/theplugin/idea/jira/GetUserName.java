package com.atlassian.theplugin.idea.jira;

import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

public class GetUserName  extends DialogWrapper {
	private JTextField userName;
	private JPanel panel;

	public GetUserName(String issueKey) {
		super(false);
		init();
		setOKActionEnabled(false);
		userName.getDocument().addDocumentListener(new DocumentListener() {

			public void insertUpdate(DocumentEvent e) {
				update();
			}

			public void removeUpdate(DocumentEvent e) {
				update();
			}

			public void changedUpdate(DocumentEvent e) {
				update();
			}

			private void update() {
				setOKActionEnabled(userName.getText().length() > 0);
			}
		});
		setTitle("New Assignee For " + issueKey);
	}

	public String getName() {
		return userName.getText();
	}
	
	protected JComponent createCenterPanel() {
		return panel;
	}

	public JComponent getPreferredFocusedComponent() {
		return userName;
	}
}
