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
import net.sf.nachocalendar.CalendarFactory;
import net.sf.nachocalendar.components.CalendarPanel;
import net.sf.nachocalendar.model.DateSelectionModel;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Jacek Jaroczynski
 */
public class DatePicker extends DialogWrapper {

	private JPanel panel = new JPanel();

	private CalendarPanel calendar;
	private JPanel bottomPanel = new JPanel();

	public DatePicker(final String title, final Date dateToSelect) {
		super(false);
		init();

		setTitle(title);

		createCalendar(dateToSelect);

		createPanel();
	}

	private void createCalendar(final Date dateToSelect) {
		calendar = CalendarFactory.createCalendarPanel(1);
		calendar.setSelectionMode(DateSelectionModel.SINGLE_SELECTION);

		Calendar nowcal = Calendar.getInstance();

		// calculate time to midnight
		nowcal.setTime(dateToSelect);
		nowcal.set(Calendar.HOUR_OF_DAY, 0);
		nowcal.set(Calendar.MINUTE, 0);
		nowcal.set(Calendar.SECOND, 0);
		nowcal.set(Calendar.MILLISECOND, 0);

		Date nowZeroZero = nowcal.getTime();

		calendar.setDate(nowZeroZero);
		calendar.setValue(nowZeroZero);
	}

	private void createPanel() {
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

		gbc.gridy = 2;
		panel.add(bottomPanel, gbc);
	}

	@Override
	protected JComponent createCenterPanel() {
		return panel;
	}

	/**
	 * @return JPanel which can hold additional controls (for inheritance purposes)
	 */
	protected JPanel getPanelComponent() {
		return bottomPanel;
	}

	/**
	 * @return Date selected in the calendar control
	 */
	public Date getSelectedDate() {
		return (Date) calendar.getValue();
	}
}
