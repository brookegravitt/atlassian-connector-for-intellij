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

import com.atlassian.theplugin.jira.api.JIRAActionField;
import com.atlassian.theplugin.jira.api.JIRAIssue;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jacek Jaroczynski
 */
public class FieldTimeTracking extends JPanel implements ActionFieldEditor {

	private static final float WARNING_FONT_SIZE = 10.0f;
	private static final int BOX_WIDTH = 5;

	private FieldTextField textField;
	private JIRAIssue issue;

	public FieldTimeTracking(final String text, final JIRAIssue issue, final JIRAActionField field) {
		super();

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		textField = new FieldTextField(text, field);
		add(textField);
		add(Box.createRigidArea(new Dimension(BOX_WIDTH, 0)));
		JLabel warningLabel = new JLabel(
				"The format of this is ' *w *d *h *m ' (weeks, days, hours and minutes - where * can be any number)");
		warningLabel.setFont(warningLabel.getFont().deriveFont(WARNING_FONT_SIZE));
		add(warningLabel);

		this.issue = issue;
	}

	public JIRAActionField getEditedFieldValue() {
		return textField.getEditedFieldValue();
	}

	public Component getComponent() {
		return this;
	}

	public String getFieldName() {
		if (issue.getTimeSpent() == null) {
			return "Original Estimate";
		}

		return "Remaining Estimate";
	}
}
