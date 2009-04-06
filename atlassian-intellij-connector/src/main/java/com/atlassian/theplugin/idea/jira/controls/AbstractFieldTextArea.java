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
import com.atlassian.theplugin.jira.api.JIRAActionFieldBean;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jacek Jaroczynski
 */
public abstract class AbstractFieldTextArea extends JScrollPane implements ActionFieldEditor {
	private JIRAActionField field;
	private JTextArea textArea;

	public AbstractFieldTextArea(final String contentText, final JIRAActionField field) {

		this.field = field;

		textArea = new JTextArea(contentText);

		textArea.setRows(8);
		textArea.setColumns(36);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		this.setViewportView(textArea);
		this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

	}

	public JIRAActionField getEditedFieldValue() {
		JIRAActionField ret = new JIRAActionFieldBean(field);
		ret.addValue(textArea.getText());
		return ret;
	}

	public Component getComponent() {
		return this;
	}
}
