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

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.jira.JIRAIssueProgressTimestampCache;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.util.PluginUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vcs.changes.ui.MultipleChangeListBrowser;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.UIUtil;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import net.sf.nachocalendar.CalendarFactory;
import net.sf.nachocalendar.components.CalendarPanel;
import net.sf.nachocalendar.model.DateSelectionModel;
import org.jetbrains.annotations.NotNull;

import javax.management.timer.Timer;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WorkLogCreateAndMaybeDeactivateDialog extends DialogWrapper {

	private JPanel contentPane;
	private JTextArea comment;
	private JButton endDateChange;
	private JLabel endDateLabel;
	private HyperlinkLabel helpLabel;
	private JTextField timeSpentField;
	private JRadioButton btnLeaveUnchanged;
	private JRadioButton btnAutoUpdate;
	private JRadioButton btnUpdateManually;
	private JTextField remainingEstimateField;
	private JTextPane anEstimateOfHowTextPane;
	private JPanel endTimePanel;
	private JCheckBox chkDeactivateChangeSet;
	private JPanel changesetPanel;
	private JPanel changesPanel;
	private JPanel timePanel;
	private JPanel commentPanel;
	private JCheckBox chkLogWork;
	private JCheckBox chkCommitChanges;
	private JPanel wrapperPanel;
	private final Project project;
	private final boolean deactivateActiveIssue;
	private Date endTime = Calendar.getInstance().getTime();

	private WdhmInputListener timeSpentListener;
	private WdhmInputListener remainingEstimateListener;
	private MultipleChangeListBrowser changesBrowserPanel;

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		createUIComponents();
		contentPane = new JPanel();
		contentPane.setLayout(new FormLayout("fill:d:grow", "center:d:grow,top:3dlu:noGrow,center:max(d;4px):noGrow"));
		contentPane.setMinimumSize(new Dimension(700, 400));
		contentPane.setPreferredSize(new Dimension(800, 600));
		wrapperPanel = new JPanel();
		wrapperPanel.setLayout(new FormLayout("fill:m:noGrow,fill:d:grow", "center:max(d;4px):noGrow,center:max(d;4px):noGrow,fill:p:grow,top:3dlu:noGrow,center:max(d;20dlu):grow(2.0)"));
		wrapperPanel.setMinimumSize(new Dimension(1, 200));
		wrapperPanel.setPreferredSize(new Dimension(1, 200));
		wrapperPanel.setRequestFocusEnabled(true);
		CellConstraints cc = new CellConstraints();
		contentPane.add(wrapperPanel, cc.xy(1, 1, CellConstraints.DEFAULT, CellConstraints.FILL));
		timePanel = new JPanel();
		timePanel.setLayout(new FormLayout("fill:2dlu:noGrow,fill:136px:noGrow,fill:max(d;4px):noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:d:noGrow,top:3dlu:noGrow,center:p:noGrow,top:3dlu:noGrow,center:d:noGrow,top:3dlu:noGrow,center:d:noGrow,top:3dlu:noGrow,center:d:noGrow,top:3dlu:noGrow,center:d:noGrow,top:3dlu:noGrow,center:d:noGrow"));
		wrapperPanel.add(timePanel, cc.xy(1, 3, CellConstraints.LEFT, CellConstraints.FILL));
		timePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Time Tracking"));
		timeSpentField = new JTextField();
		timeSpentField.setMinimumSize(new Dimension(100, 28));
		timeSpentField.setPreferredSize(new Dimension(150, 28));
		timePanel.add(timeSpentField, new CellConstraints(3, 1, 2, 1, CellConstraints.LEFT, CellConstraints.DEFAULT, new Insets(0, 0, 0, 48)));
		final JLabel label1 = new JLabel();
		label1.setText("Time Spent:");
		timePanel.add(label1, cc.xy(2, 1));
		anEstimateOfHowTextPane = new JTextPane();
		anEstimateOfHowTextPane.setEditable(false);
		anEstimateOfHowTextPane.setEnabled(true);
		anEstimateOfHowTextPane.setFont(new Font(anEstimateOfHowTextPane.getFont().getName(), anEstimateOfHowTextPane.getFont().getStyle(), 10));
		anEstimateOfHowTextPane.setMargin(new Insets(0, 12, 0, 0));
		anEstimateOfHowTextPane.setMaximumSize(new Dimension(350, 100));
		anEstimateOfHowTextPane.setMinimumSize(new Dimension(350, 100));
		anEstimateOfHowTextPane.setOpaque(false);
		anEstimateOfHowTextPane.setPreferredSize(new Dimension(350, 100));
		anEstimateOfHowTextPane.setText("An estimate of how much time \nyou have spent working. \nThe format of this is ' *w *d *h *m ' \n(representing weeks, days, hours and minutes \n- where * can be any number) \nExamples: 4d, 5h 30m, 60m and 3w. ");
		timePanel.add(anEstimateOfHowTextPane, cc.xyw(4, 3, 2, CellConstraints.FILL, CellConstraints.FILL));
		final JLabel label2 = new JLabel();
		label2.setText("Remaining Estimate:");
		timePanel.add(label2, cc.xy(2, 7));
		btnAutoUpdate = new JRadioButton();
		btnAutoUpdate.setSelected(true);
		btnAutoUpdate.setText("Auto Update");
		timePanel.add(btnAutoUpdate, cc.xy(4, 7));
		btnLeaveUnchanged = new JRadioButton();
		btnLeaveUnchanged.setText("Leave Unchanged");
		timePanel.add(btnLeaveUnchanged, cc.xy(4, 9));
		btnUpdateManually = new JRadioButton();
		btnUpdateManually.setEnabled(true);
		btnUpdateManually.setText("Update Remaining Estimate Manually:");
		timePanel.add(btnUpdateManually, cc.xy(4, 11));
		remainingEstimateField = new JTextField();
		remainingEstimateField.setEnabled(false);
		remainingEstimateField.setMaximumSize(new Dimension(150, 28));
		remainingEstimateField.setMinimumSize(new Dimension(100, 28));
		remainingEstimateField.setPreferredSize(new Dimension(150, 28));
		timePanel.add(remainingEstimateField, new CellConstraints(4, 13, 1, 1, CellConstraints.CENTER, CellConstraints.DEFAULT, new Insets(0, 48, 0, 0)));
		endTimePanel = new JPanel();
		endTimePanel.setLayout(new FormLayout("fill:d:noGrow,left:41dlu:noGrow,fill:128px:noGrow,left:9dlu:noGrow,fill:p:noGrow", "center:d:noGrow"));
		timePanel.add(endTimePanel, cc.xyw(2, 5, 3, CellConstraints.LEFT, CellConstraints.DEFAULT));
		endDateChange = new JButton();
		endDateChange.setText("Change");
		endTimePanel.add(endDateChange, cc.xy(5, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
		endDateLabel = new JLabel();
		endDateLabel.setText("1/01/08 12:00");
		endTimePanel.add(endDateLabel, cc.xy(3, 1));
		final JLabel label3 = new JLabel();
		label3.setText("End Time:");
		label3.setVerticalAlignment(1);
		label3.setVerticalTextPosition(1);
		endTimePanel.add(label3, cc.xy(1, 1));
		changesetPanel = new JPanel();
		changesetPanel.setLayout(new FormLayout("fill:p:grow", "fill:p:grow,top:3dlu:noGrow,center:d:noGrow"));
		changesetPanel.setBackground(SystemColor.control);
		changesetPanel.setMinimumSize(new Dimension(0, 16));
		changesetPanel.setPreferredSize(new Dimension(0, 16));
		changesetPanel.setRequestFocusEnabled(true);
		wrapperPanel.add(changesetPanel, cc.xy(2, 3));
		changesetPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Changes"));
		chkDeactivateChangeSet = new JCheckBox();
		chkDeactivateChangeSet.setMaximumSize(new Dimension(1, 23));
		chkDeactivateChangeSet.setMinimumSize(new Dimension(1, 23));
		chkDeactivateChangeSet.setOpaque(false);
		chkDeactivateChangeSet.setPreferredSize(new Dimension(1, 23));
		chkDeactivateChangeSet.setSelected(true);
		chkDeactivateChangeSet.setText("Deactivate Change List After Commit");
		changesetPanel.add(chkDeactivateChangeSet, cc.xy(1, 3));
		changesPanel = new JPanel();
		changesPanel.setLayout(new BorderLayout(0, 0));
		changesPanel.setBackground(SystemColor.control);
		changesPanel.setMaximumSize(new Dimension(500, 2147483647));
		changesPanel.setMinimumSize(new Dimension(0, 16));
		changesPanel.setOpaque(false);
		changesPanel.setPreferredSize(new Dimension(300, 1));
		changesetPanel.add(changesPanel, cc.xy(1, 1));
		commentPanel = new JPanel();
		commentPanel.setLayout(new FormLayout("fill:2dlu:noGrow,left:4dlu:noGrow,left:46dlu:noGrow,fill:d:grow,fill:max(d;4px):noGrow", "fill:d:grow(4.0)"));
		commentPanel.setMinimumSize(new Dimension(100, 81));
		commentPanel.setPreferredSize(new Dimension(100, 81));
		wrapperPanel.add(commentPanel, cc.xyw(1, 5, 2, CellConstraints.FILL, CellConstraints.FILL));
		final JScrollPane scrollPane1 = new JScrollPane();
		scrollPane1.setHorizontalScrollBarPolicy(30);
		commentPanel.add(scrollPane1, cc.xy(4, 1, CellConstraints.FILL, CellConstraints.FILL));
		comment = new JTextArea();
		scrollPane1.setViewportView(comment);
		final JLabel label4 = new JLabel();
		label4.setText("Comment:");
		commentPanel.add(label4, cc.xyw(2, 1, 2, CellConstraints.DEFAULT, CellConstraints.TOP));
		chkLogWork = new JCheckBox();
		chkLogWork.setSelected(true);
		chkLogWork.setText("Log Work");
		wrapperPanel.add(chkLogWork, cc.xy(1, 1));
		chkCommitChanges = new JCheckBox();
		chkCommitChanges.setMinimumSize(new Dimension(0, 23));
		chkCommitChanges.setPreferredSize(new Dimension(1, 23));
		chkCommitChanges.setSelected(true);
		chkCommitChanges.setText("Commit Changes");
		wrapperPanel.add(chkCommitChanges, cc.xy(2, 1));
		contentPane.add(helpLabel, cc.xy(1, 3));
		ButtonGroup buttonGroup;
		buttonGroup = new ButtonGroup();
		buttonGroup.add(btnUpdateManually);
		buttonGroup.add(btnAutoUpdate);
		buttonGroup.add(btnLeaveUnchanged);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return contentPane;
	}

	private class WdhmInputListener implements DocumentListener {

		private static final String REGEX = "^\\s*(\\d+w)?\\s*(\\d+d)?\\s*(\\d+h)?\\s*(\\d+m)?\\s*$";

		private JTextField field;
		private boolean matchFound;

		public WdhmInputListener(JTextField field) {
			this.field = field;
		}

		private class Period {
			public Period(String r) {
				regex = r;
				interval = 0;
			}

			public long getInterval() {
				return interval;
			}

			private long interval;

			public String getRegex() {
				return regex;
			}

			private String regex;

			public void findAndSet(String txt) {
				Pattern p = Pattern.compile("(\\d+)" + regex);
				Matcher m = p.matcher(txt);
				if (m.matches()) {
					String subs = txt.substring(m.start(1), m.end(1));
					interval = Long.valueOf(subs);
				}
			}
		}

		private Period weeks = new Period("w");
		private Period days = new Period("d");
		private Period hours = new Period("h");
		private Period minutes = new Period("m");

		public void insertUpdate(DocumentEvent e) {
			stateChanged();
		}

		public void removeUpdate(DocumentEvent e) {
			stateChanged();
		}

		public void changedUpdate(DocumentEvent e) {
			stateChanged();
		}

		public void stateChanged() {
			if (!field.isEnabled()) {
				return;
			}

			String text = field.getText();

			Pattern p = Pattern.compile(REGEX);
			Matcher m = p.matcher(text);
			Color c;

			matchFound = m.matches() && text.length() > 0;
			c = matchFound ? Color.BLACK : Color.RED;
			field.setForeground(c);
			updateOKAction();

			if (matchFound) {
				weeks.findAndSet(text);
				days.findAndSet(text);
				hours.findAndSet(text);
				minutes.findAndSet(text);
			}
		}

		public long getWeeks() {
			return weeks.interval;
		}

		public long getDays() {
			return days.interval;
		}

		public long getHours() {
			return hours.interval;
		}

		public long getMinutes() {
			return minutes.interval;
		}

		public boolean isOk() {
			return matchFound;
		}
	}

	private void createUIComponents() {
		// Help for Log Work has mysteriously disappeared from CAC. Hiding the help link for now
		helpLabel = new HyperlinkLabel("");
		helpLabel.setEnabled(false);
//		helpLabel = new HyperlinkLabel("Help");
//		final String helpUrl = HelpUrl.getHelpUrl(Constants.HELP_JIRA_WORKLOG);
//
//		helpLabel.addHyperlinkListener(new HyperlinkListener() {
//			public void hyperlinkUpdate(HyperlinkEvent e) {
//				BrowserUtil.launchBrowser(helpUrl);
//			}
//		});
	}

	private void updateOKAction() {
		boolean enable = timeSpentListener.isOk();
		if (remainingEstimateField.isEnabled() && enable) {
			enable = remainingEstimateListener.isOk();
		}
		setOKActionEnabled(enable || (deactivateActiveIssue && !chkLogWork.isSelected()));
	}

	private static final long MILLIS_IN_HOUR = 1000 * 3600;
	private static final long MILLIS_IN_MINUTE = 1000 * 60;
	private static final long MAX_ALLOWED_HOURS = 5;

	@NotNull
	public static String getFormatedDurationString(Date startTime) {
		String result = "";

		Date currentTime = new Date();
		long timediff = currentTime.getTime() - startTime.getTime();
		if (timediff <= 0) {
			return result;
		}

		long hours = timediff / MILLIS_IN_HOUR;

		// if somebody works without a break for more than 5 hours, then they are mutants and I don't serve mutants :)
		if (hours < MAX_ALLOWED_HOURS) {
			if (hours > 0) {
				result += Long.valueOf(hours).toString() + "h";
			}

			long minutes = (timediff % MILLIS_IN_HOUR) / MILLIS_IN_MINUTE;

			if (minutes > 0) {
				if (hours > 0) {
					result += " ";
				}
				result += Long.valueOf(minutes).toString() + "m";
			}
		}
		return result;
	}

	public WorkLogCreateAndMaybeDeactivateDialog(final JiraServerCfg jiraServer, final JIRAIssue issue,
												 final Project project, final String timeSpent,
												 boolean deactivateActiveIssue) {
		super(false);

		this.project = project;
		this.deactivateActiveIssue = deactivateActiveIssue;

		$$$setupUI$$$();
		if (deactivateActiveIssue) {
			setTitle("Deactivate Issue " + issue.getKey());

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ChangeListManager changeListManager = ChangeListManager.getInstance(project);
					LocalChangeList chList = changeListManager.getDefaultChangeList();

					changesBrowserPanel = IdeaVersionFacade.getInstance()
							.getChangesListBrowser(project, changeListManager, chList.getChanges());
					changesPanel.add(changesBrowserPanel, BorderLayout.CENTER);
					changesPanel.validate();
				}
			});


			getOKAction().putValue(Action.NAME, "Deactivate Issue");
		} else {
			setTitle("Add Worklog for " + issue.getKey());
			getOKAction().putValue(Action.NAME, "Add Worklog");
		}
		setOKActionEnabled(false);

		timeSpentListener = new WdhmInputListener(timeSpentField);
		remainingEstimateListener = new WdhmInputListener(remainingEstimateField);

		timeSpentField.getDocument().addDocumentListener(timeSpentListener);
		timeSpentField.setText(timeSpent);

		Date startProgressTimestamp = JIRAIssueProgressTimestampCache.getInstance().getTimestamp(jiraServer, issue);
		if (startProgressTimestamp != null) {
			timeSpentField.setText(getFormatedDurationString(startProgressTimestamp));
		}

		remainingEstimateField.getDocument().addDocumentListener(remainingEstimateListener);

		final Calendar now = Calendar.getInstance();
		endTime = now.getTime();

		endDateLabel.setText(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(now.getTime()));

		endDateChange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					//we catch NPE because of bug in  CalendaRpanel.java method  private boolean isShowing(Date date);
					//null minDate. minDate sometines is not initialized see PL-1105
					TimeDatePicker tdp = new TimeDatePicker(endTime);
					if (tdp.isOK()) {
						endTime = tdp.getSelectedTime();
						String s = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(endTime);
						endDateLabel.setText(s);
					}
				} catch (NullPointerException npe) {
					PluginUtil.getLogger().error("Cannot create TimeDatePicker object, NPE: " + npe.getMessage());
				}
			}
		});

		btnUpdateManually.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				boolean b = btnUpdateManually.isSelected();
				remainingEstimateField.setEnabled(b);
				updateOKAction();
			}
		});

		if (jiraServer == null) {
			Messages.showErrorDialog(project, "There is no selected JIRA Server", "Error");
			return;
		}

		chkLogWork.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UIUtil.setEnabled(timePanel, chkLogWork.isSelected(), true);
				setCommentsPanelVisible();
				if (timePanel.isEnabled()) {
					remainingEstimateField.setEnabled(btnUpdateManually.isSelected());
				}
			}
		});
		chkCommitChanges.addActionListener((new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UIUtil.setEnabled(changesetPanel, chkCommitChanges.isSelected(), true);
				setCommentsPanelVisible();
			}
		}));

		init();
		updateOKAction();
		setCommentText();
		validate();
		setChangelistPanelVisible();
	}

	private void setChangelistPanelVisible() {
		chkCommitChanges.setVisible(deactivateActiveIssue);
		chkLogWork.setVisible(deactivateActiveIssue);
		if (!deactivateActiveIssue) {
			wrapperPanel.remove(changesetPanel);
			timePanel.setBorder(BorderFactory.createEmptyBorder());
			Dimension newSize = new Dimension(
					(int) timePanel.getMinimumSize().getWidth(), (int) contentPane.getMinimumSize().getHeight());
			contentPane.setPreferredSize(newSize);
			contentPane.setMinimumSize(newSize);
			contentPane.setSize(contentPane.getMinimumSize());
		}
		endTimePanel.setVisible(!deactivateActiveIssue);
	}

	private void setCommentText() {
		if (deactivateActiveIssue) {
			ChangeListManager changeListManager = ChangeListManager.getInstance(project);
			LocalChangeList chList = changeListManager.getDefaultChangeList();
			comment.setText(chList.getComment());
		}
	}

	private void setCommentsPanelVisible() {
		boolean wasVisible = commentPanel.isVisible();
		commentPanel.setVisible(chkLogWork.isSelected() || chkCommitChanges.isSelected());
		if (wasVisible != commentPanel.isVisible()) {
			int height = commentPanel.getHeight();
			int plusminus = commentPanel.isVisible() ? 1 : -1;
			contentPane.setSize(contentPane.getWidth(), contentPane.getHeight() + height * plusminus);
		}
	}

	public boolean isLogTime() {
		return chkLogWork.isSelected();
	}

	public boolean isCommitChanges() {
		return chkCommitChanges.isSelected();
	}

	public boolean isDeactivateCurrentChangeList() {
		return chkDeactivateChangeSet.isSelected();
	}

	public LocalChangeList getCurrentChangeList() {
		return (LocalChangeList) changesBrowserPanel.getSelectedChangeList();
	}

	public java.util.List<Change> getSelectedChanges() {
		return changesBrowserPanel.getCurrentIncludedChanges();
	}

	public String getTimeSpentString() {
		return timeSpentField.getText();
	}

	public String getRemainingEstimateString() {
		return remainingEstimateField.getText();
	}

	public Date getEndDate() {
		return (Date) endTime.clone();
	}

	public Date getStartDate() {
		Date d = endTime;
		long t = d.getTime() - (timeSpentListener.getWeeks() * Timer.ONE_WEEK) - (timeSpentListener.getDays() * Timer.ONE_DAY)
				- (timeSpentListener.getHours() * Timer.ONE_HOUR) - (timeSpentListener.getMinutes() * Timer.ONE_MINUTE);
		d.setTime(t);
		return d;
	}

	public String getComment() {
		return comment.getText();
	}

	public boolean getAutoUpdateRemaining() {
		return btnAutoUpdate.isSelected();
	}

	public boolean getLeaveRemainingUnchanged() {
		return btnLeaveUnchanged.isSelected();
	}

	public boolean getUpdateRemainingManually() {
		return btnUpdateManually.isSelected();
	}

	@Override
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

		@Override
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
