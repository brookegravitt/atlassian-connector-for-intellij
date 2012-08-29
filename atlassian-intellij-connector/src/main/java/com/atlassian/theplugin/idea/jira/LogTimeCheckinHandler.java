package com.atlassian.theplugin.idea.jira;

import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.JIRAIssueProgressTimestampCache;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.IdeaVersionFacade;
import com.atlassian.theplugin.idea.NullCheckinHandler;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.changes.ui.CommitChangeListDialog;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.List;

/**
 * User: kalamon
 * Date: May 22, 2009
 * Time: 11:29:44 AM
 */
public class LogTimeCheckinHandler /*extends CheckinHandlerFactor*/ {
    private JiraWorkspaceConfiguration jiraCfg;

	public LogTimeCheckinHandler(@Nullable final JiraWorkspaceConfiguration jiraWorkspaceConfiguration) {
		this.jiraCfg = jiraWorkspaceConfiguration;
	}


	@NotNull
	public CheckinHandler createHandler(CheckinProjectPanel checkinProjectPanel) {
        //reflection
		// PL-1604 - the only way to detect that we are in the "Commit" dialog and not in the
		// "Create Patch" dialog seems to be the fact that the VCS list has non-zero length
		if (IdeaVersionFacade.getInstance().getAffectedVcsesSize((CommitChangeListDialog) checkinProjectPanel) > 0) {
			return new Handler(jiraCfg, checkinProjectPanel);
		}
		return new NullCheckinHandler();
	}

    private class Handler extends CheckinHandler {
        private final JiraWorkspaceConfiguration config;

        private CheckinProjectPanel checkinProjectPanel;
		private JCheckBox cbLogTime = new JCheckBox("Log Time Spent");
		private JTextField txtTimeSpent = new JTextField();
		private JTextField txtReminingEstimateHidden = new JTextField();
		private JLabel lblRemainingEstimateAdjust = new JLabel();
		private JButton btnChange = new JButton("Change");

        private JPanel panel;

        private RefreshableOnComponent afterCheckinConfig = new AfterCheckinConfiguration();
		private JiraTimeWdhmTextFieldListener timeSpentListener;
		private JiraTimeWdhmTextFieldListener timeReminingListener;
		private boolean timeSpentCorrect;
		private boolean timeReminingCorrect;

		public Handler(@Nullable final JiraWorkspaceConfiguration jiraConfig, final CheckinProjectPanel checkinProjectPanel) {
            config = jiraConfig != null ? jiraConfig : IdeaHelper.getJiraWorkspaceConfiguration(checkinProjectPanel.getProject());

			this.checkinProjectPanel = checkinProjectPanel;
			cbLogTime.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					config.setLogWorkOnCommit(cbLogTime.isSelected());
					setFieldsEnabled(cbLogTime.isSelected());
				}
			});
			timeSpentListener = new JiraTimeWdhmTextFieldListener(txtTimeSpent, false) {
				@Override
				public boolean stateChanged() {
					timeSpentCorrect = super.stateChanged();
					return timeSpentCorrect;
				}
			};
			timeReminingListener = new JiraTimeWdhmTextFieldListener(txtReminingEstimateHidden, false) {
				@Override
				public boolean stateChanged() {
					timeReminingCorrect = super.stateChanged();
					return timeReminingCorrect;
				}
			};

			ActiveJiraIssue ai = ActiveIssueUtils.getActiveJiraIssue(checkinProjectPanel.getProject());
			if (ai != null) {
				String val = StringUtil.generateJiraLogTimeString(ai.recalculateTimeSpent());
				txtTimeSpent.setText(val);
				timeSpentCorrect = val != null && val.length() > 0;
			}

			txtTimeSpent.getDocument().addDocumentListener(timeSpentListener);
			txtReminingEstimateHidden.getDocument().addDocumentListener(timeReminingListener);
			timeReminingCorrect = !config.getRemainingEstimateUpdateMode().equals(RemainingEstimateUpdateMode.MANUAL);

			RemainingEstimateUpdateMode mode = config.getRemainingEstimateUpdateMode();
			if (mode == null) {
				mode = RemainingEstimateUpdateMode.AUTO;
				config.setRemainingEstimateUpdateMode(mode);
			}
			lblRemainingEstimateAdjust.setText(mode.getText());
			lblRemainingEstimateAdjust.setForeground(timeReminingCorrect ? Color.BLACK : Color.RED);

			btnChange.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					ActiveJiraIssue ai = ActiveIssueUtils.getActiveJiraIssue(checkinProjectPanel.getProject());
					JiraServerData server = ActiveIssueUtils.getJiraServer(checkinProjectPanel.getProject());
					if (ai != null && server != null) {
						try {
							JiraIssueAdapter issue = ActiveIssueUtils.getJIRAIssue(server, ai);
							WorkLogCreateAndMaybeDeactivateDialog dlg = new WorkLogCreateAndMaybeDeactivateDialog(
									server, issue, checkinProjectPanel.getProject(), txtTimeSpent.getText().trim(), false, config);
							dlg.setRemainingEstimateUpdateMode(config.getRemainingEstimateUpdateMode());
							dlg.setRemainingEstimateString(txtReminingEstimateHidden.getText());
							dlg.show();
							if (dlg.isOK()) {
								String txt = dlg.getRemainingEstimateUpdateMode().getText();
								if (dlg.getRemainingEstimateUpdateMode().equals(RemainingEstimateUpdateMode.MANUAL)) {
									txt += dlg.getRemainingEstimateString();
								} else {
									timeReminingCorrect = true;
								}
								lblRemainingEstimateAdjust.setText(txt);
								txtTimeSpent.setText(dlg.getTimeSpentString());
								txtReminingEstimateHidden.setText(dlg.getRemainingEstimateString());
								lblRemainingEstimateAdjust.setForeground(timeReminingCorrect ? Color.BLACK : Color.RED);
								config.setRemainingEstimateUpdateMode(dlg.getRemainingEstimateUpdateMode());
							}
						} catch (JIRAException e) {
							DialogWithDetails.showExceptionDialog(
									checkinProjectPanel.getProject(), "Unable to retrieve active issue", e);
						}
					}
				}
			});
			cbLogTime.setEnabled(ActiveIssueUtils.getActiveJiraIssue(checkinProjectPanel.getProject()) != null);
			afterCheckinConfig.restoreState();
			setFieldsEnabled(cbLogTime.isSelected());
		}

		private void setFieldsEnabled(boolean enabled) {
			txtTimeSpent.setEnabled(enabled);
			lblRemainingEstimateAdjust.setEnabled(enabled);
			btnChange.setEnabled(enabled);
		}

        public RefreshableOnComponent getAfterCheckinConfigurationPanel(Disposable parentDisposable) {
			return afterCheckinConfig;
		}

		public ReturnResult beforeCheckin(@Nullable CommitExecutor commitExecutor) {
			if (cbLogTime.isEnabled() && cbLogTime.isSelected()) {
				if (!timeSpentCorrect) {
					Messages.showErrorDialog(checkinProjectPanel.getComponent(),
                        "Incorrect \"Time Spent\" value", "Error");
				} else if (!timeReminingCorrect) {
					Messages.showErrorDialog(checkinProjectPanel.getComponent(),
                        "Incorrect \"Remaining Estimate\" value", "Error");
				}
				return timeSpentCorrect && timeReminingCorrect ? ReturnResult.COMMIT : ReturnResult.CANCEL;
			}
			return ReturnResult.COMMIT;
		}


		@Override
		public void checkinSuccessful() {
			if (cbLogTime.isEnabled() && cbLogTime.isSelected()) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						logWork();
					}
				});
			}
		}

		private void logWork() {
			final Calendar cal = Calendar.getInstance();

			final ActiveJiraIssue activeIssue = ActiveIssueUtils.getActiveJiraIssue(checkinProjectPanel.getProject());
			final JiraServerData server = ActiveIssueUtils.getJiraServer(checkinProjectPanel.getProject());
			if (activeIssue != null && server != null) {
				try {
					// ok, this sucks a bit. I am creating a phony dialog just to
					// make it return the start time, based on time spent and now()
					// can't be bother to do something more intelligent though :P
					final JiraIssueAdapter issue = ActiveIssueUtils.getJIRAIssue(server, activeIssue);
					WorkLogCreateAndMaybeDeactivateDialog dlg = new WorkLogCreateAndMaybeDeactivateDialog(
							server, issue, checkinProjectPanel.getProject(),
							txtTimeSpent.getText().trim(), false, config);
					cal.setTime(dlg.getStartDate());

					final String newRemainingEstimate = config.getRemainingEstimateUpdateMode()
							.equals(RemainingEstimateUpdateMode.MANUAL)
							? txtReminingEstimateHidden.getText() : null;
					Task.Backgroundable task = new Task.Backgroundable(
							checkinProjectPanel.getProject(), "Logging Time", false) {

						public void run(@NotNull ProgressIndicator progressIndicator) {
							try {
								IntelliJJiraServerFacade.getInstance().logWork(server, issue,
                                        txtTimeSpent.getText(), cal, null,
										!config.getRemainingEstimateUpdateMode()
												.equals(RemainingEstimateUpdateMode.UNCHANGED),
										newRemainingEstimate);
								JIRAIssueProgressTimestampCache.getInstance().setTimestamp(server, issue);
								activeIssue.resetTimeSpent();
							} catch (final JIRAException e) {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										DialogWithDetails.showExceptionDialog(
												checkinProjectPanel.getProject(),
												"Failed to log time", e);
									}
								});
							}
						}
					};
					ProgressManager.getInstance().run(task);

				} catch (final JIRAException e) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							DialogWithDetails.showExceptionDialog(
									checkinProjectPanel.getProject(),
									"Failed to set starting time while logging time", e);
						}
					});
				}
			}
		}

		@Override
		public void checkinFailed(List<VcsException> vcsExceptions) {
		}

		private class AfterCheckinConfiguration implements RefreshableOnComponent {

            public JComponent getComponent() {
                if (panel == null) {
                    panel = new JPanel(new FormLayout("10dlu, fill:pref:grow, pref", "pref, pref"));
                    CellConstraints cc = new CellConstraints();

                    panel.add(cbLogTime, cc.xyw(1, 1, 2));
                    panel.add(txtTimeSpent, cc.xy(1 + 2, 1));
                    panel.add(lblRemainingEstimateAdjust, cc.xy(2, 2));
                    panel.add(btnChange, cc.xy(1 + 2, 2));
                }
				return panel;
			}

			public void refresh() {
			}

			public void saveState() {
				if (ActiveIssueUtils.getActiveJiraIssue(checkinProjectPanel.getProject()) != null) {
					config.setLogWorkOnCommit(cbLogTime.isSelected());
				}
			}

			public void restoreState() {
				cbLogTime.setSelected(
						config.isLogWorkOnCommit()
								&& ActiveIssueUtils.getActiveJiraIssue(checkinProjectPanel.getProject()) != null);
			}
		}
	}
}
