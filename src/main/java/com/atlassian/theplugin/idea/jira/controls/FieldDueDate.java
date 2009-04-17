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

import javax.swing.*;
import java.awt.*;

/**
 * @author Jacek Jaroczynski
 */
public class FieldDueDate extends JPanel implements ActionFieldEditor {
	private FieldTextField textField;
	private static final int BOX_WIDTH = 5;

	public FieldDueDate(final String date, final JIRAActionField field) {

		super();

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		textField = new FieldTextField(date, field);
		add(textField);
		add(Box.createRigidArea(new Dimension(BOX_WIDTH, 0)));

	}

	public JIRAActionField getEditedFieldValue() {
		return textField.getEditedFieldValue();
	}

	public Component getComponent() {
		return this;
	}

	public String getFieldName() {
		return textField.getFieldName();
	}
}
