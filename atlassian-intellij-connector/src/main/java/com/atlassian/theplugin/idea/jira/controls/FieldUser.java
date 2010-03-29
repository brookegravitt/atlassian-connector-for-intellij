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
package com.atlassian.theplugin.idea.jira.controls;

import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.idea.ui.UserEditLabel;
import com.intellij.openapi.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * @author Jacek Jaroczynski
 */
public class FieldUser extends JPanel implements ActionFieldEditor {
	private JComboBox comboBox = new JComboBox();
	private static final int BOX_WIDTH = 5;
	private static final String UNASSIGNED_ID = "-1";
	private JIRAActionField field;

	public FieldUser(final JIRAServerModel jiraServerModel, final JiraIssueAdapter issue, final String text,
		final JIRAActionField field) {
		super();
		this.field = field;

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		comboBox.setEditable(true);
		add(comboBox);
		add(Box.createRigidArea(new Dimension(BOX_WIDTH, 0)));

        for (Pair user : jiraServerModel.getUsers(issue.getJiraServerData())) {
	        comboBox.addItem(new UserEditLabel.UserComboBoxItem(new User((String) user.getFirst(), (String) user.getSecond())));
			if (text.equals(user.getFirst())) {
				comboBox.setSelectedIndex(comboBox.getItemCount() - 1);
			}
        }
	}

	public JIRAActionField getEditedFieldValue() {
		String selectedUser = "";
		if (comboBox.getSelectedItem() instanceof UserEditLabel.UserComboBoxItem) {
			selectedUser = ((UserEditLabel.UserComboBoxItem) comboBox.getSelectedItem()).getUser().getUsername();
		} else if (comboBox.getSelectedItem() instanceof String) {
			selectedUser = (String) comboBox.getSelectedItem();
		}
		if (selectedUser.equals(UNASSIGNED_ID)) {
			selectedUser = "";
		}
		field.setValues(Arrays.asList(selectedUser));
		return field;
	}

	public Component getComponent() {
		return this;
	}

	public String getFieldName() {
		return field.getName();
	}
}
