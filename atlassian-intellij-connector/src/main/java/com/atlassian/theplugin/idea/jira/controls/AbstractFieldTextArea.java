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

/**
 * @author Jacek Jaroczynski
 */
public abstract class AbstractFieldTextArea extends JTextArea implements ActionFieldEditor {
	public AbstractFieldTextArea(final String contentText) {
		setRows(6);
		setColumns(22);
		setLineWrap(true);
		setWrapStyleWord(true);

		setText(contentText);
	}

	public JIRAActionField getEditedFieldValue(final JIRAActionField field) {
		field.addValue(getText());
		return field;
	}
}
