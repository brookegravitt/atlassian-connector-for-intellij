package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.util.StringUtil;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.action.issues.activetoolbar.ActiveIssueUtils;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.jira.JIRAIssueProgressTimestampCache;
import com.atlassian.theplugin.jira.JIRAServerFacadeImpl;
import com.atlassian.theplugin.jira.api.JIRAException;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.ui.Messages;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.Calendar;
import java.util.List;

/**
 * User: kalamon
 * Date: May 22, 2009
 * Time: 11:29:44 AM
 */
public class LogTimeCheckinHandlerFactory extends CheckinHandlerFactory {
    private JiraWorkspaceConfiguration config;

    public LogTimeCheckinHandlerFactory(@NotNull final JiraWorkspaceConfiguration jiraWorkspaceConfiguration) {
        this.config = jiraWorkspaceConfiguration;
    }

    @NotNull
    public CheckinHandler createHandler(CheckinProjectPanel checkinProjectPanel) {
        return new Handler(checkinProjectPanel);
    }

    private class Handler extends CheckinHandler {
        private CheckinProjectPanel checkinProjectPanel;
        private JCheckBox cbLogTime = new JCheckBox("Log Time Spent");
        private JTextField txtTimeSpent = new JTextField();
        private JTextField txtReminingEstimateHidden = new JTextField();
        private JLabel lblRemainingEstimateAdjust = new JLabel();
        private JButton btnChange = new JButton("Change");

        private RefreshableOnComponent afterCheckinConfig = new AfterCheckinConfiguration();
        private JiraTimeWdhmTextFieldListener timeSpentListener;
        private JiraTimeWdhmTextFieldListener timeReminingListener;
        private boolean timeSpentCorrect;
        private boolean timeReminingCorrect;

        public Handler(final CheckinProjectPanel checkinProjectPanel) {
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
                    JiraServerCfg server = ActiveIssueUtils.getJiraServer(checkinProjectPanel.getProject());
                    if (ai != null && server != null) {
                        try {
                            JIRAIssue issue = ActiveIssueUtils.getJIRAIssue(server.getServerData(), ai);
                            WorkLogCreateAndMaybeDeactivateDialog dlg = new WorkLogCreateAndMaybeDeactivateDialog(
                                    server.getServerData(), issue, checkinProjectPanel.getProject(),
                                    txtTimeSpent.getText(), false, config);
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

        @Override
        public RefreshableOnComponent getAfterCheckinConfigurationPanel() {
            return afterCheckinConfig;
        }

        @Override
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
        public ReturnResult beforeCheckin() {
            return beforeCheckin(null);
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
            final JiraServerCfg server = ActiveIssueUtils.getJiraServer(checkinProjectPanel.getProject());
            if (activeIssue != null && server != null) {
                try {
                    // ok, this sucks a bit. I am creating a phony dialog just to
                    // make it return the start time, based on time spent and now()
                    // can't be bother to do something more intelligent though :P
                    final JIRAIssue issue = ActiveIssueUtils.getJIRAIssue(server.getServerData(), activeIssue);
                    WorkLogCreateAndMaybeDeactivateDialog dlg = new WorkLogCreateAndMaybeDeactivateDialog(
                            server.getServerData(), issue, checkinProjectPanel.getProject(),
                            txtTimeSpent.getText(), false, config);
                    cal.setTime(dlg.getStartDate());

                    final String newRemainingEstimate = config.getRemainingEstimateUpdateMode()
                            .equals(RemainingEstimateUpdateMode.MANUAL)
                            ? txtReminingEstimateHidden.getText() : null;
                    Task.Backgroundable task = new Task.Backgroundable(
                            checkinProjectPanel.getProject(), "Logging Time", false) {

                        public void run(@NotNull ProgressIndicator progressIndicator) {
                            try {
                                JIRAServerFacadeImpl.getInstance().logWork(server.getServerData(),
                                        issue, txtTimeSpent.getText(), cal, null,
                                        !config.getRemainingEstimateUpdateMode()
                                                .equals(RemainingEstimateUpdateMode.UNCHANGED),
                                        newRemainingEstimate);
                                JIRAIssueProgressTimestampCache.getInstance().setTimestamp(server.getServerData(), issue);
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
                JPanel p = new JPanel(new FormLayout("10dlu, fill:pref:grow, pref", "pref, pref"));
                CellConstraints cc = new CellConstraints();

                p.add(cbLogTime, cc.xyw(1, 1, 2));
                p.add(txtTimeSpent, cc.xy(1 + 2, 1));
                p.add(lblRemainingEstimateAdjust, cc.xy(2, 2));
                p.add(btnChange, cc.xy(1 + 2, 2));

                return p;
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
