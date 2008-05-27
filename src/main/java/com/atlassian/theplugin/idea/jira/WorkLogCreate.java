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
import com.atlassian.theplugin.idea.Constants;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.text.DateFormat;
import javax.management.timer.Timer;

import net.sf.nachocalendar.components.CalendarPanel;
import net.sf.nachocalendar.model.DateSelectionModel;

public class WorkLogCreate extends DialogWrapper {
	private JPanel contentPane;
	private JSpinner minutes;
	private JSpinner hours;
	private JSpinner days;
	private JSpinner weeks;
	private JTextArea comment;
	private JButton endDateChange;
	private JLabel endDateLabel;
	private Date endTime;
	private final Calendar now = Calendar.getInstance();
	SpinnerNumberModel weekModel = new SpinnerNumberModel(0, 0, null, 1);
	SpinnerNumberModel dayModel = new SpinnerNumberModel(0, 0, null, 1);
	SpinnerNumberModel hourModel = new SpinnerNumberModel(0, 0, null, 1);
	SpinnerNumberModel minuteModel = new SpinnerNumberModel(0, 0, null, 1);

	class NonZeroChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			boolean nonZero =
					(((Integer) weeks.getValue()).intValue()
							+ ((Integer) days.getValue()).intValue()
							+ ((Integer) hours.getValue()).intValue()
							+ ((Integer) minutes.getValue()).intValue()) > 0;
			setOKActionEnabled(nonZero);
		}
	}

	public WorkLogCreate(String issueKey) {
		super(false);
		init();
		setOKActionEnabled(false);

		setTitle("Add Worklog for " + issueKey);
		getOKAction().putValue(Action.NAME, "Add Worklog");

		weeks.setModel(weekModel);
		days.setModel(dayModel);
		hours.setModel(hourModel);
		minutes.setModel(minuteModel);

		NonZeroChangeListener listener = new NonZeroChangeListener();
		weeks.addChangeListener(listener);
		days.addChangeListener(listener);
		hours.addChangeListener(listener);
		minutes.addChangeListener(listener);

		endTime = now.getTime();

		endDateLabel.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(now.getTime()));

		endDateChange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TimeDatePicker tdp = new TimeDatePicker(endTime);
				if (tdp.isOK()) {
					endTime = tdp.getSelectedTime();
					String s = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(endTime);
					endDateLabel.setText(s);
				}
			}
		});
	}

	public String getTimeSpentString() {
		StringBuffer b = new StringBuffer();
		b.append(weeks.getValue().toString()).append("w");
		b.append(days.getValue().toString()).append("d");
		b.append(hours.getValue().toString()).append("h");
		b.append(minutes.getValue().toString()).append("m");
		return b.toString();
	}

	public Date getEndDate() {
		return (Date) endTime.clone();
	}

	public Date getStartDate() {
		Date d = endTime;
		long t = d.getTime()
				- (weekModel.getNumber().longValue() * Timer.ONE_WEEK)
				- (dayModel.getNumber().longValue() * Timer.ONE_DAY)
				- (hourModel.getNumber().longValue() * Timer.ONE_HOUR)
				- (minuteModel.getNumber().longValue() * Timer.ONE_MINUTE);
		d.setTime(t);
		return d;
	}

	public String getComment() {
		return comment.getText();
	}

	protected JComponent createCenterPanel() {
		return contentPane;
	}

	private class TimeDatePicker extends DialogWrapper {

		private JPanel panel = new JPanel();

		private CalendarPanel calendar;
		private JSpinner hour = new JSpinner();
		private JSpinner minute = new JSpinner();
		private SpinnerNumberModel hourModel;
		private SpinnerNumberModel minuteModel;


		TimeDatePicker(Date now) {
			super(false);
			init();

			calendar = new CalendarPanel(1);
			calendar.setSelectionMode(DateSelectionModel.SINGLE_SELECTION);

			Date nowZeroZero = (Date) now.clone();
			nowZeroZero.setHours(0);
			nowZeroZero.setMinutes(0);
			nowZeroZero.setSeconds(0);

			calendar.setDate(nowZeroZero);
			calendar.setValue(nowZeroZero);

			Calendar cal = Calendar.getInstance();
			cal.setTime(now);
			hourModel = new SpinnerNumberModel(cal.get(Calendar.HOUR_OF_DAY), 0, 24, 1);
			minuteModel = new SpinnerNumberModel(cal.get(Calendar.MINUTE), 0, 60, 1);
			hour.setModel(hourModel);
			minute.setModel(minuteModel);

			setTitle("Set End Time");

			panel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();

			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 2;
			gbc.weightx = 1;
			gbc.insets = new Insets(Constants.DIALOG_MARGIN, Constants.DIALOG_MARGIN, 0, Constants.DIALOG_MARGIN);
			panel.add(new JLabel("Day", SwingConstants.CENTER), gbc);
			gbc.gridy = 1;
			panel.add(calendar, gbc);

			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.gridwidth = 1;
			gbc.weightx = 0.5;
			panel.add(new JLabel("hour", SwingConstants.CENTER), gbc);
			gbc.gridy = 3;
			panel.add(hour, gbc);

			gbc.gridx = 1;
			gbc.gridy = 2;
			panel.add(new JLabel("minute", SwingConstants.CENTER), gbc);
			gbc.gridy = 3;
			panel.add(minute, gbc);
			show();
		}

		protected JComponent createCenterPanel() {
			return panel;
		}

		public Date getSelectedTime() {
			Date d = (Date) calendar.getValue();
			long newTime = d.getTime();
			newTime += hourModel.getNumber().intValue() * Timer.ONE_HOUR;
			newTime += minuteModel.getNumber().intValue() * Timer.ONE_MINUTE;
			d.setTime(newTime);
			return d;
		}
	}

}
