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
import com.atlassian.theplugin.idea.jira.ColoredTextFieldListener;
import com.atlassian.theplugin.idea.jira.DatePicker;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Jacek Jaroczynski
 */
public class FieldDueDate extends JPanel implements ActionFieldEditor {
	private FieldTextField textField;
	private static final int BOX_WIDTH = 5;
	private static final String DUE_DATE_FORMAT = "dd/MMM/yy";

	public FieldDueDate(final String date, final JIRAActionField field, final FreezeListener freezeListener) {

		super();

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

		textField = new FieldTextField(date, field);
		add(textField);
		add(Box.createRigidArea(new Dimension(BOX_WIDTH, 0)));
		final JButton button = new JButton("Select a Date");
		add(button);

		textField.getDocument().addDocumentListener(
				new LocalDateTextFieldListener(textField, freezeListener, getFieldName(), true));

		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {

				SimpleDateFormat dateFormatter = new SimpleDateFormat(DUE_DATE_FORMAT, Locale.US);
				Date dueDate;

				try {
					dueDate = dateFormatter.parse(textField.getText());
				} catch (ParseException e) {
					PluginUtil.getLogger().info("Wrong date format [" + textField.getText() + "]. Using TODAY", e);
					dueDate = new Date();
				}

				DatePicker datePicker = new DatePicker("Select Due Date", dueDate);
				datePicker.show();
				if (datePicker.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
					SimpleDateFormat dateFormat = new SimpleDateFormat(DUE_DATE_FORMAT, Locale.US);
					textField.setText(dateFormat.format(datePicker.getSelectedDate()));
				}
			}
		});
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

	private class LocalDateTextFieldListener extends ColoredTextFieldListener {
		private static final String REGEX = "^\\s*\\d{1,2}/[a-zA-Z]{3}/\\d{1,4}\\s*$";

		private FreezeListener freezeListener;
		private String fieldName;

		public LocalDateTextFieldListener(
				final FieldTextField textField, final FreezeListener freezeListener, final String fieldName,
				final boolean emptyOk) {
			super(textField, REGEX, emptyOk);

			this.freezeListener = freezeListener;
			this.fieldName = fieldName;
		}

		@Override
		public boolean stateChanged() {
			boolean isIncorrect = !super.stateChanged();

			if (isIncorrect) {
				// disable OK button
				freezeListener.fieldSyntaxError(fieldName);
			} else {
				// enable OK buttom
				freezeListener.fieldSyntaxOk(fieldName);
			}
			return !isIncorrect;
		}
	}
}
