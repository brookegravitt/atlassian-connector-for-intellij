/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
