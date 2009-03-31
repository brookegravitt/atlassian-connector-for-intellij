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
package com.atlassian.theplugin.idea.action.issues.activetoolbar;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.IssuesToolWindowPanel;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.ActiveJiraIssue;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: pmaruszak
 */
public class ActiveJiraIssueLabel extends AbstractActiveJiraIssueAction implements CustomComponentAction {
	private static final String COMPONENT_KEY = ActiveJiraIssueLabel.class.getName() + ".label";
	static final Icon JIRA_ICON = IconLoader.getIcon("/icons/jira-blue-16.png");
	static final Icon JIRA_ICON_DISABLED = IconLoader.getIcon("/icons/jira-grey-16.png");

//	public boolean displayTextInToolbar() {
//		return true;
//	}

	public void actionPerformed(final AnActionEvent event) {
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				JIRAIssue issue = getJIRAIssue(IdeaHelper.getCurrentProject(event));
//				if (issue != null) {
//					IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(event);
//					if (panel != null) {
//						panel.openIssue(issue);
//					}
//				}
//			}
//		});

	}

	public void onUpdate(final AnActionEvent event) {
	}

	public void onUpdate(final AnActionEvent event, final boolean enabled) {
		final ActiveJiraIssue issue = getActiveJiraIssue(IdeaHelper.getCurrentProject(event));
		JButton button = (JButton) event.getPresentation().getClientProperty(COMPONENT_KEY);
		if (button != null) {
			button.setText(getLabelText(issue));
			button.setEnabled(enabled);
			button.setToolTipText("Open Issue");
		}
		event.getPresentation().setEnabled(enabled);
	}

	public JComponent createCustomComponent(final Presentation presentation) {
		final JButton button = new JButton(JIRA_ICON);
		button.setText("No active issue");
		button.setBorderPainted(false);
		button.setDisabledIcon(JIRA_ICON_DISABLED);

		button.addActionListener(new ActionListener() {

			public void actionPerformed(final ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						final Project currentProject = IdeaHelper
								.getCurrentProject(DataManager.getInstance().getDataContext(button));
						final JIRAIssue issue = getJIRAIssue(currentProject);
						if (currentProject != null && issue != null) {
							final IssuesToolWindowPanel panel = IdeaHelper.getIssuesToolWindowPanel(currentProject);
							if (panel != null) {
								panel.openIssue(issue);
							}
						}

					}
				});
			}
		});

//		button.addMouseMotionListener(new MouseMotionListener(){
//
//			public void mouseDragged(final MouseEvent e) {
//			}
//
//			public void mouseMoved(final MouseEvent e) {
//				JButton button = (JButton) presentation.getClientProperty(COMPONENT_KEY);
//				if (e.getSource() instanceof JButton && button != null) {
//						JButton newButton = new JButton();
//						button.setBorder(newButton.getBorder());
//				} else if (button != null) {
//						button.setBorder(null);
//				}
//
//			}
//		});
//
//		button.addMouseListener(new MouseListener (){
//
//			public void mouseClicked(final MouseEvent e) {
//				//To change body of implemented methods use File | Settings | File Templates.
//			}
//
//			public void mousePressed(final MouseEvent e) {
//				//To change body of implemented methods use File | Settings | File Templates.
//			}
//
//			public void mouseReleased(final MouseEvent e) {
//				JButton button = (JButton) presentation.getClientProperty(COMPONENT_KEY);
//				if (button != null) {
//					button.setBorder(null);
//				}
//			}
//
//			public void mouseEntered(final MouseEvent e) {
//				//To change body of implemented methods use File | Settings | File Templates.
//			}
//
//			public void mouseExited(final MouseEvent e) {
//				JButton button = (JButton) presentation.getClientProperty(COMPONENT_KEY);
//				if (button != null) {
//					button.setBorder(null);
//				}
//			}
//		});
		presentation.putClientProperty(COMPONENT_KEY, button);
		return button;
	}
}
