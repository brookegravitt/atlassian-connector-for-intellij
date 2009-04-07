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
import com.atlassian.theplugin.jira.JiraActionFieldType;
import com.atlassian.theplugin.jira.api.JIRAActionField;
import com.atlassian.theplugin.jira.api.JIRAIssue;
import com.atlassian.theplugin.jira.model.JIRAServerModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class PerformIssueActionForm extends DialogWrapper {
	private JPanel root;
	private JPanel contentPanel;
	private Project project;
	private JIRAIssue issue;
	private List<ActionFieldEditor> createdFieldEditors = new ArrayList<ActionFieldEditor>();

	public PerformIssueActionForm(final Project project, final JIRAIssue issue, final List<JIRAActionField> fields,
			final String name) {

		super(project, true);
		this.project = project;

		this.issue = issue;

		$$$setupUI$$$();

		init();
		pack();
		createContent(fields);

		setTitle(name);
		root.setMinimumSize(new Dimension(600, 300));
//		getOKAction().putValue(Action.NAME, name);
	}

	private void createContent(final List<JIRAActionField> fieldList) {

		String columns = "3dlu, right:pref, 3dlu, fill:pref:grow, 3dlu";
		String rows = "3dlu";

		Collection<JIRAActionField> sortedFieldList = JiraActionFieldType.sortFieldList(fieldList);

		rows = createLayoutRows(sortedFieldList, rows);

		int y = 2;

		contentPanel.setLayout(new FormLayout(columns, rows));
		final CellConstraints cc = new CellConstraints();

		JIRAServerModel jiraServerModel = IdeaHelper.getProjectComponent(project, JIRAServerModel.class);

		for (JIRAActionField field : sortedFieldList) {

			ActionFieldEditor editor = null;

			switch (JiraActionFieldType.getFiledTypeForFieldId(field)) {
				case SUMMARY:
					editor = new FieldSummary(issue, field);
					break;
				case DESCRIPTION:
					editor = new FieldDescription(issue, field);
					break;
				case ENVIRONMENT:
					editor = new FieldEnvironment(issue, field);
					break;
				case ISSUE_TYPE:
					editor = new FieldIssueType(jiraServerModel, issue, field);
					break;
				case RESOLUTION:
					editor = new FieldResolution(jiraServerModel, issue, field);
					break;
				case ASSIGNEE:
					editor = new FieldUser(issue.getAssigneeId(), field);
					break;
				case PRIORITY:
					editor = new FieldPriority(jiraServerModel, issue, field);
					break;
				case VERSIONS:
					editor = new FieldAffectsVersion(jiraServerModel, issue, field);
					break;
				case FIX_VERSIONS:
					editor = new FieldFixForVersion(jiraServerModel, issue, field);
					break;
				case COMPONENTS:
					editor = new FieldComponents(jiraServerModel, issue, field);
					break;
				case REPORTER:
					editor = new FieldUser(issue.getReporterId(), field);
					break;
				case TIME_SPENT:
				case CALENDAR:
				case UNSUPPORTED:
				default:
					break;
			}

			if (editor != null) {
				final JLabel label = new JLabel(field.getName() + ":");
				contentPanel.add(label, cc.xy(2, y, CellConstraints.RIGHT, CellConstraints.TOP));
				contentPanel.add(editor.getComponent(), cc.xy(4, y));
				y += 2;

				createdFieldEditors.add(editor);
			}
		}
	}

	private String createLayoutRows(final Collection<JIRAActionField> fieldList, String rows) {
		for (JIRAActionField field : fieldList) {
			switch (JiraActionFieldType.getFiledTypeForFieldId(field)) {
				case SUMMARY:
				case ISSUE_TYPE:
				case RESOLUTION:
				case ASSIGNEE:
				case PRIORITY:
				case VERSIONS:
				case FIX_VERSIONS:
				case COMPONENTS:
				case REPORTER:
					rows += ", p, 3dlu";
					break;
				case DESCRIPTION:
				case ENVIRONMENT:
					rows += ", fill:pref:grow, 3dlu";
					break;
				case TIME_SPENT:
				case CALENDAR:
				case UNSUPPORTED:
				default:
					break;
			}
		}
		return rows;
	}


	public List<JIRAActionField> getFields() {
		List<JIRAActionField> ret = new ArrayList<JIRAActionField>();

		for (ActionFieldEditor editor : createdFieldEditors) {
			ret.add(editor.getEditedFieldValue());
		}

		return ret;
	}

	protected void doOKAction() {
		super.doOKAction();
	}

	@Nullable
	protected JComponent createCenterPanel() {
		return root;
	}

	{
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!

	}

	/**
	 * Method generated by IntelliJ IDEA GUI Designer
	 * >>> IMPORTANT!! <<<
	 * DO NOT edit this method OR call it in your code!
	 *
	 * @noinspection ALL
	 */
	private void $$$setupUI$$$() {
		root = new JPanel();
		root.setLayout(new BorderLayout(0, 0));
		final JScrollPane scrollPane1 = new JScrollPane();
		scrollPane1.setHorizontalScrollBarPolicy(31);
		root.add(scrollPane1, BorderLayout.CENTER);
		contentPanel = new JPanel();
		contentPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
		scrollPane1.setViewportView(contentPanel);
	}

	/**
	 * @noinspection ALL
	 */
	public JComponent $$$getRootComponent$$$() {
		return root;
	}
}
