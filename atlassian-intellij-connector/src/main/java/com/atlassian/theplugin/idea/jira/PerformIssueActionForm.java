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

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.controls.*;
import com.atlassian.theplugin.idea.ui.ScrollablePanel;
import com.atlassian.theplugin.jira.JiraActionFieldType;
import com.atlassian.theplugin.jira.api.JIRAActionField;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.JIRAServerModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class PerformIssueActionForm extends DialogWrapper implements FreezeListener {
	private JPanel root;
	private JPanel contentPanel;
	private Project project;
	private JIRAIssue issue;
	private List<JIRAActionField> fields;
	private List<ActionFieldEditor> createdFieldEditors = new ArrayList<ActionFieldEditor>();
	private int semaphore = 0;
	private CommentTextArea commentTextArea;

	public PerformIssueActionForm(final Project project, final JIRAIssue issue, final List<JIRAActionField> fields,
			final String name) {

		super(project, true);
		this.project = project;
		this.issue = issue;
		this.fields = fields;

		setupUI();

		init();
		pack();
		createContent(fields);

		setTitle(name);
		root.setMinimumSize(new Dimension(600, 300));
		root.setPreferredSize(new Dimension(600, 300));
//		getOKAction().putValue(Action.NAME, name);
	}

	private void createContent(final List<JIRAActionField> fieldList) {

		String columns = "3dlu, right:pref, 3dlu, fill:1dlu:grow, 3dlu";
		String rows = "3dlu";

		Collection<JIRAActionField> sortedFieldList = JiraActionFieldType.sortFieldList(fieldList);

		JIRAServerModel jiraServerModel = IdeaHelper.getProjectComponent(project, JIRAServerModel.class);

		List<ActionFieldEditor> editors = new ArrayList<ActionFieldEditor>();
		List<String> unsupportedFields = new ArrayList<String>();

		for (JIRAActionField field : sortedFieldList) {

			ActionFieldEditor editor = null;
			String row = null;

			switch (JiraActionFieldType.getFiledTypeForFieldId(field)) {
				case SUMMARY:
					editor = new FieldTextField(issue.getSummary(), field);
					row = ", pref, 3dlu";
					break;
				case DESCRIPTION:
					// we use wiki markup version from field (not html version from issue)
					editor = new FieldTextArea(field.getValues().get(0), field);
					row = ", fill:pref:grow, 3dlu";
					break;
				case ISSUE_TYPE:
					editor = new FieldIssueType(jiraServerModel, issue, field, this);
					row = ", p, 3dlu";
					break;
				case RESOLUTION:
					editor = new FieldResolution(jiraServerModel, issue, field, this);
					row = ", p, 3dlu";
					break;
				case ASSIGNEE:
					editor = new FieldUser(issue.getAssigneeId(), field);
					row = ", p, 3dlu";
					break;
				case PRIORITY:
					editor = new FieldPriority(jiraServerModel, issue, field, this);
					row = ", p, 3dlu";
					break;
				case VERSIONS:
					editor = new FieldAffectsVersion(jiraServerModel, issue, field, this);
					row = ", p, 3dlu";
					break;
				case FIX_VERSIONS:
					editor = new FieldFixForVersion(jiraServerModel, issue, field, this);
					row = ", p, 3dlu";
					break;
				case COMPONENTS:
					editor = new FieldComponents(jiraServerModel, issue, field, this);
					row = ", p, 3dlu";
					break;
				case REPORTER:
					editor = new FieldUser(issue.getReporterId(), field);
					row = ", p, 3dlu";
					break;
				case ENVIRONMENT:
					editor = new FieldTextArea(field.getValues().get(0), field);
					row = ", fill:pref:grow, 3dlu";
					break;
				case TIMETRACKING:
					editor = new FieldTimeTracking(field.getValues().get(0), issue, field);
					row = ", p, 3dlu";
					break;
				case CALENDAR:
				case UNSUPPORTED:
				default:
					unsupportedFields.add(field.getName());
					break;
			}

			if (editor != null && row != null) {
				editors.add(editor);
				rows += row;
			}
		}

		rows += ", fill:pref:grow, 3dlu";	// Comments text area

		if (!unsupportedFields.isEmpty()) {
			rows += ", pref, 3dlu";	// warning status line about not handled
		}

		contentPanel.setLayout(new FormLayout(columns, rows));
		final CellConstraints cc = new CellConstraints();

		int y = 2;

		for (ActionFieldEditor editor : editors) {
			final JLabel label = new JLabel(editor.getFieldName() + ":");
			contentPanel.add(label, cc.xy(2, y, CellConstraints.RIGHT, CellConstraints.TOP));
			contentPanel.add(editor.getComponent(), cc.xy(4, y));
			createdFieldEditors.add(editor);
			y += 2;
		}

		final JLabel label = new JLabel("Comment :");
		contentPanel.add(label, cc.xy(2, y, CellConstraints.RIGHT, CellConstraints.TOP));
		// todo create field for Comments
		commentTextArea = new CommentTextArea();
		contentPanel.add(commentTextArea, cc.xy(4, y));

		y += 2;

		if (!unsupportedFields.isEmpty()) {
			String warning = "Unsupported fields (original values copied): ";
			warning += StringUtils.join(unsupportedFields, ", ");
			contentPanel.add(new JLabel(warning), cc.xyw(2, y, 3, CellConstraints.LEFT, CellConstraints.CENTER));
		}

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				contentPanel.validate();
			}
		});
	}

	public List<JIRAActionField> getFields() {

		List<JIRAActionField> ret = new ArrayList<JIRAActionField>();

		ret.addAll(fields);

		for (ActionFieldEditor editor : createdFieldEditors) {
			if (ret.contains(editor.getEditedFieldValue())) {
				ret.remove(editor.getEditedFieldValue());
			}
			ret.add(editor.getEditedFieldValue());
		}

		return ret;
	}

	public String getComment() {
		return commentTextArea.getComment();
	}

	protected void doOKAction() {
		super.doOKAction();
	}

	@Nullable
	protected JComponent createCenterPanel() {
		return root;
	}

	public void freeze() {
		semaphore++;
		getOKAction().setEnabled(false);
		root.validate();
	}

	public void unfreeze() {
		semaphore--;
		if (semaphore == 0) {
			getOKAction().setEnabled(true);
			root.validate();
		}
	}

	private void setupUI() {
		root = new JPanel();
		root.setOpaque(false);
		root.setLayout(new BorderLayout(0, 0));
		final JScrollPane scroll = new JScrollPane();
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		root.add(scroll, BorderLayout.CENTER);
		contentPanel = new ScrollablePanel();
		scroll.setViewportView(contentPanel);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return root;
	}
}
