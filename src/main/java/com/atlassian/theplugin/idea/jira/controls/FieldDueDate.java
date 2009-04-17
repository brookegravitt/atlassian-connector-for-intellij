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
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.ui.DialogWrapper;
import net.sf.nachocalendar.CalendarFactory;
import net.sf.nachocalendar.components.CalendarPanel;
import net.sf.nachocalendar.model.DateSelectionModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
		final JButton button = new JButton("Select a Date");
		add(button);

		button.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {

				SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MMM/yy", Locale.US);
				Date dueDate = null;

				try {
					dueDate = dateFormatter.parse(textField.getText());
				} catch (ParseException e) {
					PluginUtil.getLogger().info("Wrong date format [" + textField.getText() + "]. Using TODAY", e);
					dueDate = new Date();
				}

				DatePicker datePicker = new DatePicker("Select Due Date", dueDate);
				if (datePicker.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yy", Locale.US);
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

	private class DatePicker extends DialogWrapper {

		private JPanel panel = new JPanel();

		private CalendarPanel calendar;

		DatePicker(final String title, final Date now) {
			super(false);
			init();
			Calendar nowcal = Calendar.getInstance();

			calendar = CalendarFactory.createCalendarPanel(1);
			calendar.setSelectionMode(DateSelectionModel.SINGLE_SELECTION);

			nowcal.setTime(now);
			nowcal.set(Calendar.HOUR_OF_DAY, 0);
			nowcal.set(Calendar.MINUTE, 0);
			nowcal.set(Calendar.SECOND, 0);
			nowcal.set(Calendar.MILLISECOND, 0);

			Date nowZeroZero = nowcal.getTime();

			calendar.setDate(nowZeroZero);
			calendar.setValue(nowZeroZero);

			Calendar cal = Calendar.getInstance();
			cal.setTime(now);

			setTitle(title);

			panel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();

			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 2;
			gbc.weightx = 1;
			panel.add(new JLabel("Day", SwingConstants.CENTER), gbc);
			gbc.gridy = 1;
			panel.add(calendar, gbc);

			show();
		}

		@Override
		protected JComponent createCenterPanel() {
			return panel;
		}

		public Date getSelectedDate() {
			return (Date) calendar.getValue();
		}
	}
}
