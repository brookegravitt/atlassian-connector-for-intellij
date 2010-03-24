package com.atlassian.theplugin.idea.ui;

import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.cache.CacheConstants;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.CachedIconLoader;
import com.atlassian.theplugin.jira.model.JIRAIssueListModelBuilder;
import com.atlassian.theplugin.jira.model.JIRAServerModelIdea;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * User: pstefaniak
 * Date: Mar 22, 2010
 */
public class TypeEditLabel extends JPanel {
		private EditIssueFieldButton button;
		private JComponent label;
		private final Project project;
		private final JIRAServerModelIdea cache;
		private final JiraIssueAdapter issue;
		private final JIRAIssueListModelBuilder jiraIssueListModelBuilder;

		public TypeEditLabel(Project project, JIRAIssueListModelBuilder jiraIssueListModelBuilder, JIRAServerModelIdea cache,
				JiraIssueAdapter issue) {
			this.project = project;
			this.jiraIssueListModelBuilder = jiraIssueListModelBuilder;
			this.cache = cache;
			this.issue = issue;
			button = new EditIssueFieldButton();
			this.label = new JLabel(issue.getType(),
					CachedIconLoader.getIcon(issue.getTypeIconUrl()),
					SwingConstants.LEFT);
			this.label.setBackground(Color.WHITE);
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

		private class EditIssueFieldButton extends JRadioButton {
			private final Icon editIcon = IconLoader.getIcon("/icons/edit.png");

			public EditIssueFieldButton() {
				super();
				setIcon(editIcon);
				this.setBackground(com.intellij.util.ui.UIUtil.getLabelBackground());
				this.setBorder(BorderFactory.createEmptyBorder());
				this.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						TypeListDialog dialog = new TypeListDialog(project);
						dialog.setTitle("Change Issue Type");
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

		private class TypeListDialog extends DialogWrapper {
			private JComboBox comboBox = new JComboBox();
			private JLabel label = new JLabel("Type:");
			private JPanel rootPanel = new JPanel(new BorderLayout());

			protected TypeListDialog(Project project) {
				super(project, false);
				fillComboModel();
				setOKButtonText("Change");
				setModal(true);
				comboBox.setRenderer(new JiraConstantCellRenderer());
				rootPanel.add(label, BorderLayout.WEST);
				rootPanel.add(comboBox, BorderLayout.CENTER);
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
						"Updating issue " + issue.getKey(), false) {
					@Override
					public void run(@NotNull ProgressIndicator progressIndicator) {
						String selectedType = ((Long) ((JIRAConstant) comboBox.getSelectedItem()).getId()).toString();
						try {
							IntelliJJiraServerFacade.getInstance().setType(issue.getJiraServerData(), issue, selectedType);
							jiraIssueListModelBuilder.reloadIssue(issue.getKey(), issue.getJiraServerData());
						} catch (JIRAException e) {
							e.printStackTrace();
						}
					}
				});
				super.doOKAction();
			}

			private void fillComboModel() {
				JIRAProject jiraProject = null;
				JiraServerData jiraServerData = issue.getJiraServerData();
				try {
					List<JIRAProject> projects = cache.getProjects(jiraServerData);
					for (JIRAProject candidate : projects) {
						if (issue.getProjectKey().equals(candidate.getKey())) {
							jiraProject = candidate;
							break;
						}
					}
				} catch (JIRAException e) {
					e.printStackTrace();
				}
				List<JIRAConstant> issueTypes = null;
				try {
					issueTypes = cache.getIssueTypes(jiraServerData, jiraProject, true);
				} catch (JIRAException e) {
					e.printStackTrace();
				}
				if (issueTypes == null) {
					return;
				}
				for (JIRAConstant constant : issueTypes) {
					if (constant.getId() != CacheConstants.ANY_ID) {
						comboBox.addItem(constant);
					}
				}
			}
		}
}

