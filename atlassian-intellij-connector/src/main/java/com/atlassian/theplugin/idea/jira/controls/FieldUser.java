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

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * @author Jacek Jaroczynski
 */
public class FieldUser extends JPanel implements ActionFieldEditor {
	private FieldTextField textField;
	private static final float WARNING_FONT_SIZE = 10.0f;
	private static final int BOX_WIDTH = 5;
	private static final String UNASSIGNED_NAME = "Unassigned";
	private static final String UNASSIGNED_ID = "-1";

	public FieldUser(final String text, final JIRAActionField field) {
		super();

		String userId = text;

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		if (userId.equals(UNASSIGNED_ID)) {
			userId = UNASSIGNED_NAME;
		}

		textField = new FieldTextField(userId, field);
		add(textField);
		add(Box.createRigidArea(new Dimension(BOX_WIDTH, 0)));
		JLabel warningLabel = new JLabel("Warning! This field is not validated prior to sending to JIRA");
		warningLabel.setFont(warningLabel.getFont().deriveFont(WARNING_FONT_SIZE));
		add(warningLabel);
	}

	public JIRAActionField getEditedFieldValue() {
		JIRAActionField field = textField.getEditedFieldValue();
		if (field.getValues().get(0).equals(UNASSIGNED_NAME)) {
			field.setValues(Arrays.asList(UNASSIGNED_ID));
		}
		return field;
	}

	public Component getComponent() {
		return this;
	}

	public String getFieldName() {
		return textField.getFieldName();
	}
}
