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
package com.atlassian.theplugin.idea.ui;

import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.jira.model.JIRAServerModelIdea;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author pmaruszak
 * date Feb 23, 2010
 */
public abstract class UserEditLabel extends JPanel {
    private EditIssueFieldButton button;
    private JComponent label;
    private final Project project;
    private final String dialogTitle;
    private final JIRAServerModelIdea cache;
    private final JiraIssueAdapter jiraIssue;


    public UserEditLabel(Project project, String dialogTitle, JComponent label, JIRAServerModelIdea cache,
                         JiraIssueAdapter jiraIssue) {
        this.project = project;
        this.dialogTitle = dialogTitle;
        this.cache = cache;
        this.jiraIssue = jiraIssue;
        button = new EditIssueFieldButton();
        this.label = label;
		setBackground(Color.WHITE);
		button.setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder());
        rebuild();
    }

    private void rebuild() {
        JPanel groupingPanel = new JPanel(new GridBagLayout());
        groupingPanel.setBackground(getBackground());
        groupingPanel.setBorder(BorderFactory.createEmptyBorder());


        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;

        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 0.0;
        gbc1.weighty = 0.0;
        gbc1.fill = GridBagConstraints.NONE;

        removeAll();
        if (label != null) {
            setBackground(label.getBackground());
            groupingPanel.add(label, gbc1);
        }
        gbc1.gridx++;
        groupingPanel.add(button, gbc1);
        add(groupingPanel, gbc);

        addFillerPanel(this, gbc, true);
    }

    private static void addFillerPanel(JPanel parent, GridBagConstraints gbc, boolean horizontal) {
        if (horizontal) {
            gbc.gridx++;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
        } else {
            gbc.gridy++;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.VERTICAL;
        }
        JPanel filler = new JPanel();
        filler.setBorder(BorderFactory.createEmptyBorder());
        filler.setOpaque(false);
        parent.add(filler, gbc);
    }


    public abstract void doOkAction(String selectedUserLogin) throws JIRAException;

    private class EditIssueFieldButton extends JRadioButton {
        private final Icon editIcon = IconLoader.getIcon("/icons/edit.png");

        public EditIssueFieldButton() {
            super();
            setIcon(editIcon);
            this.setBackground(com.intellij.util.ui.UIUtil.getLabelBackground());
            this.setBorder(BorderFactory.createEmptyBorder());
            this.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    UserListDialog dialog = new UserListDialog(project);
                    dialog.setTitle(dialogTitle);
                    dialog.show();
                }
            });

            this.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                public void mouseExited(MouseEvent e) {
                    setCursor(Cursor.getDefaultCursor());
                }
            });
        }
    }

    private class UserListDialog extends DialogWrapper {
        private JComboBox comboBox = new JComboBox();
        private JLabel label = new JLabel("Select user from the list or type in user login name");
        private JPanel rootPanel = new JPanel(new BorderLayout());

        protected UserListDialog(Project project) {
            super(project, false);
            fillComboModel();
            setOKButtonText("Change");
            setModal(true);
            comboBox.setEditable(true);
			label.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
			rootPanel.add(label, BorderLayout.PAGE_START);
            rootPanel.add(comboBox, BorderLayout.PAGE_END);
			rootPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            init();
        }

        @Override
        protected JComponent createCenterPanel() {

            return rootPanel;
        }

        @Override
        protected void doOKAction() {
            ProgressManager.getInstance().run(new Task.Backgroundable(project,
                    "Updating issue " + jiraIssue.getKey(), false) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        String selectedUserName = getSelectedUserName();
                        if (selectedUserName != null && selectedUserName.length() > 0) {
                            UserEditLabel.this.doOkAction(selectedUserName);
                        } else {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    Messages.showInfoMessage(project,
                                            "Please select non empty user name", "Updating issue stopped");
                                }
                            });
                        }
                    } catch (final JIRAException e) {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                DialogWithDetails.showExceptionDialog(project,
                                        "Updating issue " + jiraIssue.getKey() + " has failed", e);
                            }
                        });
                    }
                }
            });
            super.doOKAction();
        }

        private String getSelectedUserName() {
            if (comboBox.getSelectedItem() instanceof UserComboBoxItem) {
                return ((UserComboBoxItem) comboBox.getSelectedItem()).getUser().getUsername();
            } else if (comboBox.getSelectedItem() instanceof String) {
                return (String) comboBox.getSelectedItem();
            }

            return "";
        }


        private void fillComboModel() {
            for (Pair user : cache.getUsers(jiraIssue.getJiraServerData())) {
                comboBox.addItem(new UserComboBoxItem(new User((String) user.getFirst(), (String) user.getSecond())));
            }
        }


    }

    public static final class UserComboBoxItem {
        private final User user;

        public UserComboBoxItem(User user) {
            this.user = user;
        }

        @Override
        public String toString() {
            return user.getDisplayName() + " (" + user.getUsername() + ")";
        }

        public User getUser() {
            return user;
        }
    }


}
