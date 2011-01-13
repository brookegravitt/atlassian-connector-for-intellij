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
import com.atlassian.connector.commons.jira.JIRAActionFieldBean;
import com.atlassian.connector.commons.jira.JIRAIssueBean;
import com.atlassian.connector.commons.jira.beans.JIRAComponentBean;
import com.atlassian.connector.commons.jira.beans.JIRAConstant;
import com.atlassian.connector.commons.jira.beans.JIRAFixForVersionBean;
import com.atlassian.connector.commons.jira.beans.JIRAPriorityBean;
import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRASecurityLevelBean;
import com.atlassian.connector.commons.jira.beans.JIRAVersionBean;
import com.atlassian.connector.commons.jira.cache.CacheConstants;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.UiTask;
import com.atlassian.theplugin.commons.UiTaskAdapter;
import com.atlassian.theplugin.commons.jira.IntelliJJiraServerFacade;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.commons.jira.api.JiraIssueAdapter;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.commons.util.MiscUtil;
import com.atlassian.theplugin.configuration.JiraWorkspaceConfiguration;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.config.GenericComboBoxItemWrapper;
import com.atlassian.theplugin.idea.jira.controls.FieldUser;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.idea.ui.JiraConstantCellRenderer;
import com.atlassian.theplugin.idea.util.IdeaUiMultiTaskExecutor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class IssueCreateDialog extends DialogWrapper {
    private JPanel mainPanel;
    private JTextArea description;
    private JComboBox projectComboBox;
    private JComboBox typeComboBox;
    private JTextField summary;
    private JComboBox priorityComboBox;
    private FieldUser assigneeField;
    private JList componentsList;
    private JList versionsList;
    private JList fixVersionsList;
    private JTextField originalEstimate;
    private JLabel OriginalEstimateLabel;
    private JComboBox cbSecurityLevel;
    private JLabel lSecurityLevel;
    private final JiraServerData jiraServerData;
    private IssueListToolWindowPanel issueListToolWindowPanel;
    private Project project;
    private final JIRAServerModel model;
    private JiraWorkspaceConfiguration jiraConfiguration;
    private List<JIRASecurityLevelBean> securityLevels;

    public IssueCreateDialog(@NotNull IssueListToolWindowPanel issueListToolWindowPanel,
                             @NotNull Project project, JIRAServerModel model, final JiraServerData jiraServerData,
                             @NotNull final JiraWorkspaceConfiguration jiraProjectCfg) {
        super(false);
        this.issueListToolWindowPanel = issueListToolWindowPanel;
        this.project = project;
        this.model = model;
        this.jiraConfiguration = jiraProjectCfg;
        $$$setupUI$$$();
        originalEstimate.getDocument().addDocumentListener(new JiraTimeWdhmTextFieldListener(originalEstimate, true));
        assigneeField = new FieldUser(model, jiraServerData, "", null);
        mainPanel.add(assigneeField, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                new Dimension(50, -1), new Dimension(150, -1), null, 0, false));
        componentsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        init();
        pack();

        this.jiraServerData = jiraServerData;
        setTitle("Create JIRA Issue");

        projectComboBox.setRenderer(new ColoredListCellRenderer() {
            @Override
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                if (value != null) {
                    append(((JIRAProject) value).getName(), SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES);
                }
            }
        });

        typeComboBox.setRenderer(new JiraConstantCellRenderer());
        typeComboBox.setEnabled(false);

        priorityComboBox.setRenderer(new JiraConstantCellRenderer());

        projectComboBox.addPopupMenuListener(new PopupMenuListener() {

            private Object item = null;

            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                item = projectComboBox.getSelectedItem();
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
                if (item != null && item != projectComboBox.getSelectedItem()) {
                    updateProjectRelatedItems();
                }
            }

            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
                item = null;
            }
        });
        getOKAction().setEnabled(false);
        getOKAction().putValue(Action.NAME, "Create");
    }

    private boolean issueTypesUpdated = false;
    private boolean componentsUpdated = false;
    private boolean versionsUpdated = false;
    private boolean fixVersionsUpdated = false;

    private void updateProjectRelatedItems() {
        JIRAProject p = (JIRAProject) projectComboBox.getSelectedItem();
        List<UiTask> tasks = new ArrayList<UiTask>();
        issueTypesUpdated = false;
        componentsUpdated = false;
        versionsUpdated = false;
        fixVersionsUpdated = false;
        setProjectComboBoxEnableState();
        tasks.add(updateIssueTypes(p));
        tasks.add(updateComponents(p));
        tasks.add(updateVersions(p));
        tasks.add(updateFixVersions(p));
        tasks.add(updateSecurityLevels(p));
        IdeaUiMultiTaskExecutor.execute(tasks, getContentPane());
    }

    private void setProjectComboBoxEnableState() {
        projectComboBox.setEnabled(issueTypesUpdated && componentsUpdated && versionsUpdated && fixVersionsUpdated);
    }

    public void initData() {
        List<UiTask> tasks = new ArrayList<UiTask>();
        tasks.add(updatePriorities());
        tasks.add(updateProject());
        IdeaUiMultiTaskExecutor.execute(tasks, getContentPane());
    }

    private UiTaskAdapter updateProject() {
        projectComboBox.setEnabled(false);
        getOKAction().setEnabled(false);

        return new UiTaskAdapter("retrieving projects", getContentPane()) {
            private List<JIRAProject> projects = new ArrayList<JIRAProject>();

            public void run() throws JIRAException {
                projects = model.getProjects(jiraServerData);
            }

            public void onSuccess() {
                addProjects(projects);
            }

            public void onError() {
                addProjects(projects);
            }
        };
    }

    private void addProjects(List<JIRAProject> projects) {
        projectComboBox.removeAllItems();

        for (JIRAProject project : projects) {
            if (project.getId() != CacheConstants.ANY_ID) {
                projectComboBox.addItem(project);
            }
        }

        if (projectComboBox.getModel().getSize() > 0) {

            boolean defaultSelected = false;

            // select default project
            if (jiraConfiguration != null &&
                    jiraConfiguration.getView().getServerDefaultss().containsKey(jiraServerData.getServerId())) {

                String project = jiraConfiguration.getView().getServerDefaultss().
                        get(jiraServerData.getServerId()).getProject();

                for (int i = 0; i < projectComboBox.getItemCount(); ++i) {
                    if (projectComboBox.getItemAt(i) instanceof JIRAProject) {
                        if (((JIRAProject) projectComboBox.getItemAt(i)).getKey().equals(project)) {
                            projectComboBox.setSelectedIndex(i);
                            defaultSelected = true;
                            break;
                        }
                    }
                }
            }

            if (!defaultSelected) {
                projectComboBox.setSelectedIndex(0);
            }
        }
        updateProjectRelatedItems();
    }

    private UiTask updatePriorities() {
        priorityComboBox.setEnabled(false);
        getOKAction().setEnabled(false);

        return new UiTaskAdapter("retrieving priorities", getContentPane()) {
            private List<JIRAPriorityBean> priorities = new ArrayList<JIRAPriorityBean>();

            public void run() throws Exception {
                priorities = model.getPriorities(jiraServerData, myPerformAction);
            }

            public void onSuccess() {
                addIssuePriorities(priorities);
            }

            public void onError() {
                addIssuePriorities(priorities);
            }
        };
    }

    private void addIssuePriorities(List<JIRAPriorityBean> priorieties) {
        priorityComboBox.removeAllItems();
        for (JIRAConstant constant : priorieties) {
            if (constant.getId() != CacheConstants.ANY_ID) {
                priorityComboBox.addItem(constant);
            }
        }
        if (priorityComboBox.getModel().getSize() > 0) {
            priorityComboBox.setSelectedIndex(priorityComboBox.getModel().getSize() / 2);
        }
        priorityComboBox.setEnabled(true);
    }

    private UiTask updateIssueTypes(final JIRAProject project) {
        typeComboBox.setEnabled(false);
        getOKAction().setEnabled(false);
        return new UiTaskAdapter("retrieving issue types", getContentPane()) {
            private List<JIRAConstant> issueTypes = new ArrayList<JIRAConstant>();

            public void run() throws Exception {
                issueTypes = model.getIssueTypes(jiraServerData, project, true);
            }

            public void onSuccess() {
                addIssueTypes(issueTypes);
                issueTypesUpdated = true;
                setProjectComboBoxEnableState();
            }

            public void onError() {
                addIssueTypes(issueTypes);
                issueTypesUpdated = true;
                setProjectComboBoxEnableState();
            }
        };
    }

    private UiTask updateComponents(final JIRAProject project) {
        componentsList.setEnabled(false);
        getOKAction().setEnabled(false);
        return new UiTaskAdapter("fetching components", getContentPane()) {
            private List<JIRAComponentBean> components;

            public void run() throws Exception {
                components = model.getComponents(jiraServerData, project, true);
            }

            @Override
            public void onSuccess() {
                componentsUpdated = true;
                setProjectComboBoxEnableState();
                addComponents(components);
            }

            @Override
            public void onError() {
                componentsUpdated = true;
                setProjectComboBoxEnableState();
            }
        };
    }

    private UiTask updateVersions(final JIRAProject project) {
        versionsList.setEnabled(false);
        getOKAction().setEnabled(false);
        return new UiTaskAdapter("fetching versions", getContentPane()) {
            private List<JIRAVersionBean> versions;

            public void run() throws Exception {
                versions = model.getVersions(jiraServerData, project, false);
            }

            @Override
            public void onSuccess() {
                versionsUpdated = true;
                setProjectComboBoxEnableState();
                addVersions(versions);

            }

            @Override
            public void onError() {
                versionsUpdated = true;
                setProjectComboBoxEnableState();
            }
        };
    }

    private UiTask updateSecurityLevels(final JIRAProject project) {
        versionsList.setEnabled(false);
        getOKAction().setEnabled(false);
        return new UiTaskAdapter("fetching security levels", getContentPane()) {

            public void run() throws Exception {
                if (project != null) {
                    securityLevels = model.getSecurityLevels(jiraServerData, project.getKey());
                }
            }

            @Override
            public void onSuccess() {
                addSecurityLevels(securityLevels);
                if (securityLevels != null && securityLevels.size() > 0) {
                    setVisibleSecurityLevel(true);
                } else {
                    setVisibleSecurityLevel(false);
                }
            }

            @Override
            public void onError() {
                securityLevels = null;
                setVisibleSecurityLevel(false);
            }

            void setVisibleSecurityLevel(boolean visible) {
                cbSecurityLevel.setVisible(visible);
                lSecurityLevel.setVisible(visible);


            }
        };
    }

    private UiTask updateFixVersions(final JIRAProject project) {
        fixVersionsList.setEnabled(false);
        getOKAction().setEnabled(false);
        return new UiTaskAdapter("fetching versions", getContentPane()) {
            private List<JIRAFixForVersionBean> versions;

            public void run() throws Exception {
                versions = model.getFixForVersions(jiraServerData, project, false);
            }

            @Override
            public void onSuccess() {
                fixVersionsUpdated = true;
                setProjectComboBoxEnableState();
                addFixForVersions(versions);
            }

            @Override
            public void onError() {
                fixVersionsUpdated = true;
                setProjectComboBoxEnableState();
            }
        };
    }

    private void addVersions(List<JIRAVersionBean> versions) {
        versionsList.removeAll();
        final DefaultListModel listModel = new DefaultListModel();
        for (JIRAVersionBean version : versions) {
            if (version != null && version.getId() != CacheConstants.ANY_ID) {
                listModel.addElement(new VersionWrapper(version));
            }
        }
        versionsList.setModel(listModel);
        versionsList.setEnabled(true);
        getOKAction().setEnabled(true);
    }

    private void  addSecurityLevels(List<JIRASecurityLevelBean> levels) {
        if (levels != null) {
            cbSecurityLevel.removeAllItems();
            cbSecurityLevel.addItem(new SecurityLevelWrapper(new JIRASecurityLevelBean(-1L, "None"), true));
            for (JIRASecurityLevelBean level : levels) {
                cbSecurityLevel.addItem(new SecurityLevelWrapper(level));
            }
        }
    }

    private void addFixForVersions(List<JIRAFixForVersionBean> versions) {
        fixVersionsList.removeAll();
        final DefaultListModel listModel = new DefaultListModel();
        for (JIRAFixForVersionBean version : versions) {
            if (version != null && version.getId() != CacheConstants.ANY_ID) {
                listModel.addElement(new VersionWrapper(version));
            }
        }
        fixVersionsList.setModel(listModel);
        fixVersionsList.setEnabled(true);
        getOKAction().setEnabled(true);
    }

    private void addIssueTypes(List<JIRAConstant> issueTypes) {
        typeComboBox.removeAllItems();
        for (JIRAConstant constant : issueTypes) {
            if (constant.getId() != CacheConstants.ANY_ID) {
                typeComboBox.addItem(constant);
            }
        }
        typeComboBox.setEnabled(true);
        getOKAction().setEnabled(true);
    }


    private void addComponents(Collection<JIRAComponentBean> components) {
        final DefaultListModel listModel = new DefaultListModel();
        for (JIRAComponentBean constant : components) {
            if (constant != null && constant.getId() != CacheConstants.ANY_ID) {
                listModel.addElement(new ComponentWrapper(constant));
            }
        }
        componentsList.setModel(listModel);

        if (projectComboBox.getSelectedItem() != null && jiraConfiguration != null && jiraConfiguration.getView() != null
                && jiraConfiguration.getView().getServerDefaultss() != null
                && jiraConfiguration.getView().getServerDefaultss().containsKey(jiraServerData.getServerId())) {

            String selectedProject = ((JIRAProject) projectComboBox.getSelectedItem()).getKey();

            String configProject = jiraConfiguration.getView().getServerDefaultss().
                    get(jiraServerData.getServerId()).getProject();

            Collection<Long> configComponents = jiraConfiguration.getView().getServerDefaultss().
                    get(jiraServerData.getServerId()).getComponents();

            // select default components for specified project
            if (selectedProject.equals(configProject)) {

                ArrayList<Integer> indexesToSelect = new ArrayList<Integer>(componentsList.getModel().getSize() + 1);

                for (int i = 0; i < componentsList.getModel().getSize(); ++i) {
                    if (componentsList.getModel().getElementAt(i) instanceof ComponentWrapper) {
                        ComponentWrapper wrapper = (ComponentWrapper) componentsList.getModel().getElementAt(i);

                        if (wrapper.getWrapped() != null) {
                            JIRAComponentBean component = wrapper.getWrapped();

                            if (configComponents.contains(component.getId())) {
                                indexesToSelect.add(i);
                            }
                        }
                    }
                }

                if (indexesToSelect.size() > 0) {
                    componentsList.setSelectedIndices(ArrayUtils.toPrimitive(indexesToSelect.toArray(
                            new Integer[indexesToSelect.size()])));
                }
            }
        }

        componentsList.setEnabled(true);
        getOKAction().setEnabled(true);
    }


    @Override
    protected void doOKAction() {
        JIRAIssueBean newIssue;

        newIssue = new JIRAIssueBean();
        newIssue.setSummary(summary.getText());

        if (projectComboBox.getSelectedItem() == null) {
            Messages.showErrorDialog(this.getContentPane(), "Project has to be selected", "Project not defined");
            return;
        }
        newIssue.setProjectKey(((JIRAProject) projectComboBox.getSelectedItem()).getKey());
        if (typeComboBox.getSelectedItem() == null) {
            Messages.showErrorDialog(this.getContentPane(), "Issue type has to be selected", "Issue type not defined");
            return;
        }
        newIssue.setType(((JIRAConstant) typeComboBox.getSelectedItem()));
        newIssue.setDescription(description.getText());
        newIssue.setPriority(((JIRAPriorityBean) priorityComboBox.getSelectedItem()));
        newIssue.setOriginalEstimate(originalEstimate.getText());
        if (securityLevels != null && securityLevels.size() > 0 && cbSecurityLevel.getSelectedItem() != null) {
            SecurityLevelWrapper wrapper = (SecurityLevelWrapper)cbSecurityLevel.getSelectedItem();
            JIRASecurityLevelBean sl = wrapper.getWrapped();
            if (sl != null /*&& !wrapper.isNone()*/) {
                newIssue.setSecurityLevel(sl);
            }
        }

        List<JIRAConstant> components = MiscUtil.buildArrayList();
        Collection<Long> selectedComponents = new LinkedHashSet<Long>();
        for (Object selectedObject : componentsList.getSelectedValues()) {
            if (selectedObject instanceof ComponentWrapper) {
                ComponentWrapper componentWrapper = (ComponentWrapper) selectedObject;
                if (componentWrapper.getWrapped().getId() == CacheConstants.UNKNOWN_COMPONENT_ID) {
                    if (componentsList.getSelectedValues().length > 1) {
                        Messages.showErrorDialog(getContentPane(), "You cannot select \"Unknown\" with a specific component.");
                        return;
                    }
                }
                components.add(componentWrapper.getWrapped());
                selectedComponents.add(componentWrapper.getWrapped().getId());
            }
        }
        if (versionsList.getSelectedValues().length > 0) {
            List<JIRAConstant> versions = new ArrayList<JIRAConstant>();
            for (Object ver : versionsList.getSelectedValues()) {
                VersionWrapper vw = (VersionWrapper) ver;
                versions.add(vw.getWrapped());
            }
            newIssue.setAffectsVersions(versions);
        }

        if (fixVersionsList.getSelectedValues().length > 0) {
            List<JIRAConstant> versions = new ArrayList<JIRAConstant>();
            for (Object ver : fixVersionsList.getSelectedValues()) {
                VersionWrapper vw = (VersionWrapper) ver;
                versions.add(vw.getWrapped());
            }
            newIssue.setFixVersions(versions);
        }

        if (components.size() > 0) {
            newIssue.setComponents(components);
        }
        String assignTo = assigneeField.getSelectedUser();
        if (assignTo.length() > 0) {
            newIssue.setAssignee(assignTo);
        }

        // save selected project and components to the config
        if (jiraConfiguration != null && jiraConfiguration.getView() != null) {
            JIRAProject p = (JIRAProject) projectComboBox.getSelectedItem();
            jiraConfiguration.getView().addServerDefault(jiraServerData.getServerId(), p.getKey(), selectedComponents);
        }

        createIssueAndCloseOnSuccess(newIssue);
    }

    private void createIssueAndCloseOnSuccess(final JIRAIssueBean newIssue) {
        Task createTask = new Task.Modal(project, "Creating Issue", false) {

            public void run(@NotNull final ProgressIndicator indicator) {
                String message;

                indicator.setIndeterminate(true);

                try {
                    final IntelliJJiraServerFacade jiraServerFacade = IntelliJJiraServerFacade.getInstance();
                    final JiraIssueAdapter createdIssue = jiraServerFacade.createIssue(jiraServerData, newIssue);

                    IdeaHelper.getProjectCfgManager(project).addProjectConfigurationListener(createdIssue.getLocalConfigurationListener());

                    message = "New issue created: <a href="
                            + createdIssue.getIssueUrl()
                            + ">"
                            + createdIssue.getKey()
                            + "</a>";

                    issueListToolWindowPanel.setStatusInfoMessage(message);

                    if (originalEstimate.getText() != null && originalEstimate.getText().length() > 0) {
//                        "timeoriginalestimate"
                        JIRAActionField originalEstimateField = new JIRAActionFieldBean("timetracking", "Original Estimate");
                        originalEstimateField.addValue(newIssue.getOriginalEstimate());

                        ArrayList<JIRAActionField> fieldList = new ArrayList<JIRAActionField>();
                        fieldList.add(originalEstimateField);

                        jiraServerFacade.setFields(jiraServerData, createdIssue, fieldList);
                        message = "Original estimate updated for: <a href="
                                + createdIssue.getIssueUrl()
                                + ">"
                                + createdIssue.getKey()
                                + "</a>";

                        issueListToolWindowPanel.setStatusInfoMessage(message);
                    }

                    EventQueue.invokeLater(new Runnable() {
                        public void run() {

                            issueListToolWindowPanel.refreshIssues(true);
                            issueListToolWindowPanel.openIssue(createdIssue, false);

                            close(0);
                        }
                    });
                } catch (final JIRAException e) {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            DialogWithDetails.showExceptionDialog(project, "Failed to create new issue", e);
                        }
                    });
                }
            }

        };

        ProgressManager.getInstance().run(createTask);
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return summary;
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    public JPanel getRootComponent() {
        return mainPanel;
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
        mainPanel.setLayout(new GridLayoutManager(10, 3, new Insets(5, 5, 5, 5), -1, -1));
        mainPanel.setMinimumSize(new Dimension(480, 500));
        final JScrollPane scrollPane1 = new JScrollPane();
        mainPanel.add(scrollPane1, new GridConstraints(7, 1, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        description = new JTextArea();
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        scrollPane1.setViewportView(description);
        final JLabel label1 = new JLabel();
        label1.setText("Summary:");
        mainPanel.add(label1, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Project:");
        mainPanel.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        projectComboBox = new JComboBox();
        mainPanel.add(projectComboBox, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), null, null, 0, false));
        summary = new JTextField();
        mainPanel.add(summary, new GridConstraints(6, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, new Dimension(100, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Description:");
        mainPanel.add(label3, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Assignee:");
        mainPanel.add(label4, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setFont(new Font(label5.getFont().getName(), label5.getFont().getStyle(), 10));
        label5.setHorizontalTextPosition(10);
        label5.setText("Warning! This field is not validated prior to sending to JIRA");
        mainPanel.add(label5, new GridConstraints(9, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Type:");
        mainPanel.add(label6, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        typeComboBox = new JComboBox();
        mainPanel.add(typeComboBox, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Priority:");
        mainPanel.add(label7, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        priorityComboBox = new JComboBox();
        mainPanel.add(priorityComboBox, new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, new Dimension(100, -1), null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Component/s:");
        mainPanel.add(label8, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane2 = new JScrollPane();
        mainPanel.add(scrollPane2, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED,
                null, null, null, 0, false));
        componentsList = new JList();
        componentsList.setToolTipText("Select Affected Components ");
        componentsList.setVisibleRowCount(5);
        scrollPane2.setViewportView(componentsList);
        final JLabel label9 = new JLabel();
        label9.setText("Affects Version/s:");
        mainPanel.add(label9, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane3 = new JScrollPane();
        mainPanel.add(scrollPane3, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        versionsList = new JList();
        versionsList.setVisibleRowCount(5);
        scrollPane3.setViewportView(versionsList);
        final JScrollPane scrollPane4 = new JScrollPane();
        mainPanel.add(scrollPane4, new GridConstraints(4, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        fixVersionsList = new JList();
        fixVersionsList.setVisible(true);
        fixVersionsList.setVisibleRowCount(5);
        scrollPane4.setViewportView(fixVersionsList);
        final JLabel label10 = new JLabel();
        label10.setText("Fix Version/s:");
        mainPanel.add(label10, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label1.setLabelFor(summary);
        label2.setLabelFor(projectComboBox);
        label3.setLabelFor(description);
        label6.setLabelFor(typeComboBox);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    private static class ComponentWrapper extends GenericComboBoxItemWrapper<JIRAComponentBean> {

        public ComponentWrapper(@NotNull final JIRAComponentBean wrapped) {
            super(wrapped);
        }

        @Override
        public String toString() {
            return wrapped.getName();
        }
    }

    private static class VersionWrapper extends GenericComboBoxItemWrapper<JIRAVersionBean> {

        public VersionWrapper(@NotNull final JIRAVersionBean wrapped) {
            super(wrapped);
        }

        @Override
        public String toString() {
            return wrapped.getName();
        }
    }

    private static class SecurityLevelWrapper extends GenericComboBoxItemWrapper<JIRASecurityLevelBean> {
        boolean none = false;

        public SecurityLevelWrapper(final JIRASecurityLevelBean wrapped) {
            super(wrapped);
        }

        public SecurityLevelWrapper(final JIRASecurityLevelBean wrapped, boolean none) {
            super(wrapped);
            this.none = none;
        }

        @Override
        public String toString() {
            return wrapped.getName() + " (" + wrapped.getId() + ")";
        }
        public boolean isNone() {
            return none;
        }
    }
}