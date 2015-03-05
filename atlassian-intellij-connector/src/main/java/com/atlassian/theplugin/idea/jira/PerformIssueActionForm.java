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

import com.atlassian.connector.commons.jira.JIRAActionField;
import com.atlassian.connector.commons.jira.JiraCustomField;
import com.atlassian.theplugin.commons.jira.JiraActionFieldType;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.jira.controls.ActionFieldEditor;
import com.atlassian.theplugin.idea.jira.controls.CommentTextArea;
import com.atlassian.theplugin.idea.jira.controls.FieldAffectsVersion;
import com.atlassian.theplugin.idea.jira.controls.FieldComponents;
import com.atlassian.theplugin.idea.jira.controls.FieldDueDate;
import com.atlassian.theplugin.idea.jira.controls.FieldFixForVersion;
import com.atlassian.theplugin.idea.jira.controls.FieldIssueType;
import com.atlassian.theplugin.idea.jira.controls.FieldPriority;
import com.atlassian.theplugin.idea.jira.controls.FieldResolution;
import com.atlassian.theplugin.idea.jira.controls.FieldTextArea;
import com.atlassian.theplugin.idea.jira.controls.FieldTextField;
import com.atlassian.theplugin.idea.jira.controls.FieldTimeTracking;
import com.atlassian.theplugin.idea.jira.controls.FieldUser;
import com.atlassian.theplugin.idea.jira.controls.FreezeListener;
import com.atlassian.theplugin.idea.ui.ScrollablePanel;
import com.atlassian.theplugin.idea.util.Html2text;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class PerformIssueActionForm extends DialogWrapper implements FreezeListener {
    private JPanel root;
    private JPanel contentPanel;
    private Project project;
    private JiraIssueAdapter issue;
    private List<JIRAActionField> fields;
    private HashMap<String, Boolean> fieldsStatus = Maps.newHashMap();
    private List<ActionFieldEditor> createdFieldEditors = Lists.newArrayList();
    private int semaphore = 0;
    private CommentTextArea commentTextArea;

    public PerformIssueActionForm(final Project project, final JiraIssueAdapter issue, final List<JIRAActionField> fields,
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

        JIRAServerModel jiraServerModel = IdeaHelper.getJIRAServerModel(project);

        List<ActionFieldEditor> editors = Lists.newArrayList();
        List<String> unsupportedFields = Lists.newArrayList();

        for (JIRAActionField field : sortedFieldList) {

            ActionFieldEditor editor = null;
            String row = null;

            switch (JiraActionFieldType.getFieldTypeForFieldId(field)) {
                case SUMMARY:
                    editor = new FieldTextField(issue.getSummary(), field);
                    row = ", pref, 3dlu";
                    break;
                case DESCRIPTION:
					editor = new FieldTextArea(Html2text.translate(issue.getWikiDescription()), field);
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
                    editor = new FieldUser(jiraServerModel, issue.getJiraServerData(), issue.getAssigneeId(), field);
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
                    editor = new FieldUser(jiraServerModel, issue.getJiraServerData(), issue.getReporterId(), field);
                    row = ", p, 3dlu";
                    break;
                case ENVIRONMENT:
//                    editor = new FieldTextArea(field.getValues().get(0), field);
                    editor = new FieldTextArea(Html2text.translate(issue.getEnvironment()), field);
                    row = ", fill:pref:grow, 3dlu";
                    break;
                case TIMETRACKING:
                    editor = new FieldTimeTracking(field.getValues().get(0), issue, field, this);
                    row = ", p, 3dlu";
                    break;
                case DUE_DATE:
                    String content = "";
                    if (field.getValues() != null && field.getValues().size() > 0) {
                        content = field.getValues().get(0);
                    }
                    editor = new FieldDueDate(content, field, this);
                    row = ", p, 3dlu";
                    break;
                case CUSTOM_FIELD:
                    for (JiraCustomField custom : issue.getCustomFields()) {
                        if (custom.getId().equals(field.getFieldId())) {
                            List<String> values = custom.getValues();
                            String val = values != null && values.size() > 0 ? values.get(0) : null;
                            switch (custom.getTypeKey()) {
                                case NUMERIC:
                                    editor = new FieldTextField(val, field);
                                    row = ", p, 3dlu";
                                    break;
                                case TEXT:
                                    editor = new FieldTextField(val, field);
                                    row = ", p, 3dlu";
                                    break;
                                case TEXT_AREA:
//                                    editor = new FieldEditorPane(custom.getValues().get(0).replaceAll("<br/>", "\n"), field, true);
                                    editor = new FieldTextArea(val != null ? val.replaceAll("<br/>", "") : "", field);
                                    row = ", p, 3dlu";
                                    break;
                                case DATE_PICKER:
                                    editor = new FieldDueDate(custom.getFormattedValue(), field, this);
                                    row = ", p, 3dlu";
                                    break;
                                case URL:
                                    editor = new FieldTextField(custom.getFormattedValue(), field);
                                    row = ", p, 3dlu";
                                case UNSUPPORTED:
                                default:
                                    break;
                            }
                        }
                    }
                    break;
                case UNSUPPORTED:
                default:
                    unsupportedFields.add(field.getName());
                    break;
            }

            if (editor != null) {
                editors.add(editor);
                rows += row;
            }
        }

        rows += ", fill:pref:grow, 3dlu";    // Comments text area

        if (!unsupportedFields.isEmpty()) {
            rows += ", pref, 3dlu";    // warning status line about not handled
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
            String warning = issue.usesRest()
                ? "Unsupported fields (skipped): "
                : "Unsupported fields (original values copied): ";
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

        if (issue.usesRest()) {
            // just fill in supported fields for REST
            for (ActionFieldEditor editor : createdFieldEditors) {
                ret.add(editor.getEditedFieldValue());
            }
        } else {
            ret.addAll(fields);

            for (ActionFieldEditor editor : createdFieldEditors) {
                if (ret.contains(editor.getEditedFieldValue())) {
                    ret.remove(editor.getEditedFieldValue());
                }
                ret.add(editor.getEditedFieldValue());
            }
        }

        return ret;
    }

    public String getComment() {
        return commentTextArea.getComment();
    }

    protected void doOKAction() {
        // PL-1784 - healing the sympthoms. I have absolutely no clue what could cause it. Could be some EAP brokenness
        try {
            super.doOKAction();
        } catch (NullPointerException e) {
            LoggerImpl.getInstance().error(e);
        }
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

    public void fieldSyntaxError(final String fieldName) {
        if (fieldsStatus.containsKey(fieldName)) {
            if (!fieldsStatus.get(fieldName)) {
                fieldsStatus.put(fieldName, true);
                semaphore++;
            }

        } else {
            fieldsStatus.put(fieldName, true);
            semaphore++;
        }

        if (semaphore > 0) {
            getOKAction().setEnabled(false);
        }
    }

    public void fieldSyntaxOk(final String fieldName) {
        if (fieldsStatus.containsKey(fieldName)) {
            if (fieldsStatus.get(fieldName)) {
                fieldsStatus.put(fieldName, false);
                semaphore--;
            }
        }

        if (semaphore == 0) {
            getOKAction().setEnabled(true);
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
