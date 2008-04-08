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

/*
 * Created by IntelliJ IDEA.
 * User: amrk
 * Date: 16/03/2004
 * Time: 21:00:20
 */
package com.atlassian.theplugin.idea.jira;

import com.atlassian.theplugin.bamboo.HtmlBambooStatusListener;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BuildStatusChangedToolTip;
import com.atlassian.theplugin.jira.JIRAServer;
import com.atlassian.theplugin.jira.JIRAServerFacade;
import com.atlassian.theplugin.jira.api.*;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.Nullable;
import thirdparty.javaworld.ClasspathHTMLEditorKit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IssueCreate extends DialogWrapper {
	private static final Logger LOGGER = Logger.getInstance("IssueCreate");

	private JPanel mainPanel;
	private JTextArea description;
	private JComboBox projectComboBox;
	private JComboBox typeComboBox;
	private JTextField summary;
	private final JIRAServer jiraServer;
	private final JIRAServerFacade jiraServerFacade;

	public IssueCreate(JIRAServer jiraServer, JIRAServerFacade jiraServerFacade) {
		super(false);
		this.jiraServerFacade = jiraServerFacade;
		init();
		this.jiraServer = jiraServer;
		setTitle("Create JIRA Issue");

		projectComboBox.setRenderer(new ColoredListCellRenderer() {
			protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
				append(((JIRAProject) value).getName(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
			}
		});

		typeComboBox.setRenderer(new ColoredListCellRenderer() {
			protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
				JIRAConstant type = (JIRAConstant) value;
				append(type.getName(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
				setIcon(CachedIconLoader.getIcon(type.getIconUrl()));
			}
		});

		for (JIRAProject project : jiraServer.getProjects()) {
			projectComboBox.addItem(project);
		}

		for (JIRAConstant constant : jiraServer.getIssueTypes()) {
			typeComboBox.addItem(constant);
		}

		getOKAction().putValue(Action.NAME, "Create");
	}

	protected void doOKAction() {
		JIRAIssueBean issueProxy = new JIRAIssueBean();
		issueProxy.setSummary(summary.getText());
		issueProxy.setProjectKey(((JIRAProject) projectComboBox.getSelectedItem()).getKey());
		issueProxy.setType(((JIRAConstant) typeComboBox.getSelectedItem()));
		issueProxy.setDescription(description.getText());
		JEditorPane content = new JEditorPane();

		JIRAIssue newIssue = null;

		String message = null;
		try {
			newIssue = jiraServerFacade.createIssue(jiraServer.getServer(), issueProxy);
		} catch (JIRAException e) {
			message = "Issue created failed?<br>" + e.getMessage();
			content.setBackground(BuildStatusChangedToolTip.BACKGROUND_COLOR_FAILED);
			e.printStackTrace();
		}

		if (newIssue != null) {
			message = "<table width=100% height=100%><tr><td valign=center align=center>"
					+ "<b style=\"font-size: 24pt;\"><a href='" + newIssue.getIssueUrl() + "'>"
					+ newIssue.getKey() + "</a><br>created</b></td></tr></table>";
			content.setBackground(BuildStatusChangedToolTip.BACKGROUND_COLOR_SUCCEED);
		}

		final JIRAIssue innerIssue = newIssue;

		content.setEditable(false);
		content.setContentType("text/html");
		content.setEditorKit(new ClasspathHTMLEditorKit());
		content.setText("<html>" + HtmlBambooStatusListener.BODY_WITH_STYLE + message + "</body></html>");
		content.addHyperlinkListener(new GenericHyperlinkListener());
		content.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				BrowserUtil.launchBrowser(innerIssue.getIssueUrl());
			}
		});
		content.setCaretPosition(0); // do thi to make sure scroll pane is always at the top / header
		WindowManager.getInstance().getStatusBar(IdeaHelper.getCurrentProject()).fireNotificationPopup(
				new JScrollPane(content), null);

		super.doOKAction();
	}

	public JComponent getPreferredFocusedComponent() {
		return summary;
	}

	@Nullable
	protected JComponent createCenterPanel() {
		return mainPanel;
	}

	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
		$$$setupUI$$$();
	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayoutManager(4, 4, new Insets(5, 5, 5, 5), -1, -1));
		mainPanel.setMinimumSize(new Dimension(400, 100));
		final JScrollPane scrollPane1 = new JScrollPane();
		mainPanel.add(scrollPane1, new GridConstraints(3, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
		description = new JTextArea();
		description.setLineWrap(true);
		description.setMinimumSize(new Dimension(439, 120));
		description.setPreferredSize(new Dimension(439, 120));
		description.setWrapStyleWord(true);
		scrollPane1.setViewportView(description);
		final JLabel label1 = new JLabel();
		label1.setText("Summary:");
		mainPanel.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label2 = new JLabel();
		label2.setText("Project:");
		mainPanel.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		projectComboBox = new JComboBox();
		mainPanel.add(projectComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		summary = new JTextField();
		mainPanel.add(summary, new GridConstraints(1, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
		final JLabel label3 = new JLabel();
		label3.setText("Type:");
		mainPanel.add(label3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		typeComboBox = new JComboBox();
		mainPanel.add(typeComboBox, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		final JLabel label4 = new JLabel();
		label4.setText("Description:");
		mainPanel.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
		label1.setLabelFor(summary);
		label2.setLabelFor(projectComboBox);
		label3.setLabelFor(typeComboBox);
		label4.setLabelFor(description);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return mainPanel;
	}
}