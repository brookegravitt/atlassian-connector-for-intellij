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

import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.idea.jira.renderers.JIRAConstantListRenderer;
import com.atlassian.theplugin.idea.jira.renderers.JIRAQueryFragmentListRenderer;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.atlassian.theplugin.commons.jira.api.*;
import com.atlassian.theplugin.commons.jira.api.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerCache;
import com.atlassian.theplugin.commons.jira.cache.JIRAServerModel;
import com.atlassian.theplugin.jira.model.JIRAFilterListModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class JiraIssuesFilterPanel extends DialogWrapper {
    private JList projectList;
    private JList issueTypeList;
    private JList fixForList;
    private JList affectsVersionsList;
    private JComboBox reporterComboBox;
    private JComboBox assigneeComboBox;
    private JList resolutionsList;
    private JList prioritiesList;
    private JList statusList;
    private JPanel rootPanel;
    private JScrollPane prioritiesScrollPane;
    private JScrollPane resolutionScrollPane;
    private JScrollPane statusScrollPane;
    private JScrollPane affectVersionScrollPane;
    private JScrollPane fixForScrollPane;
    private JScrollPane projectScrollPane;
    private JScrollPane issueTypeScrollPane;
    private JScrollPane componentsScrollPane;
    private JList componentsList;
    private JLabel componentsLabel;
    private JLabel fixForLabel;
    private JLabel reporterLabel;
    private JLabel assigneeLabel;
    private JLabel statusLabel;
    private JLabel resolutionsLabel;
    private JLabel prioritiesLabel;
    private JLabel affectsVersionsLabel;


    private final JIRAServerModel jiraServerModel;
    private final JIRAFilterListModel filterListModel;
    private JIRAProject currentJiraProject;
    private ServerData jiraServerCfg;
    private FilterActionClear clearFilterAction = new FilterActionClear();
    private List<JIRAQueryFragment> initialFilter = new ArrayList<JIRAQueryFragment>();
    private boolean windowClosed;

    public JiraIssuesFilterPanel(final Project project, final JIRAServerModel jiraServerModel,
                                 final JIRAFilterListModel filterListModel, final ServerData jiraServerCfg) {

        super(project, false);
        this.jiraServerModel = jiraServerModel;
        this.filterListModel = filterListModel;
        this.jiraServerCfg = jiraServerCfg;
        $$$setupUI$$$();
        setModal(true);
        setTitle("Configure Custom JIRA Filter");
        init();
        pack();

        this.projectList.setCellRenderer(new JIRAQueryFragmentListRenderer());
        this.issueTypeList.setCellRenderer(new JIRAConstantListRenderer());
        this.statusList.setCellRenderer(new JIRAConstantListRenderer());
        this.prioritiesList.setCellRenderer(new JIRAConstantListRenderer());
        this.resolutionsList.setCellRenderer(new JIRAQueryFragmentListRenderer());
        this.fixForList.setCellRenderer(new JIRAQueryFragmentListRenderer());
        this.componentsList.setCellRenderer(new JIRAQueryFragmentListRenderer());
        this.affectsVersionsList.setCellRenderer(new JIRAQueryFragmentListRenderer());
        this.reporterComboBox.setRenderer(new JIRAQueryFragmentListRenderer());
        this.assigneeComboBox.setRenderer(new JIRAQueryFragmentListRenderer());
    }

    private void addProjectActionListener() {
        projectList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    int size = projectList.getSelectedValues().length;
                    switch (size) {
                        case 0:
                            currentJiraProject = null;
                            refreshProjectDependentLists();
                            break;
                        case 1:
                            currentJiraProject = (JIRAProjectBean) projectList.getSelectedValues()[0];
                            refreshProjectDependentLists();
                            break;
                        default:
                            currentJiraProject = null;
                            clearProjectDependentLists();
                            refreshGlobalIssueTypeList();
                            break;
                    }
                }
            }
        });
    }

    // CHECKSTYLE:OFF

    @Override
    protected Action[] createActions() {
        return new Action[]{getOKAction(), clearFilterAction, getCancelAction()};
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(2, 3, new Insets(10, 10, 10, 10), -1, -1));
        rootPanel.setFocusCycleRoot(false);
        rootPanel.setMaximumSize(new Dimension(-1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Components / Versions");
        rootPanel.add(label1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        fixForLabel = new JLabel();
        fixForLabel.setFont(new Font(fixForLabel.getFont().getName(), fixForLabel.getFont().getStyle(), 11));
        fixForLabel.setText("Fix For:");
        panel1.add(fixForLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(66, 14), null, 0, false));
        componentsLabel = new JLabel();
        componentsLabel.setFont(new Font(componentsLabel.getFont().getName(), componentsLabel.getFont().getStyle(), 11));
        componentsLabel.setHorizontalAlignment(4);
        componentsLabel.setText("Components:");
        panel1.add(componentsLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(66, 14), null, 0, false));
        affectsVersionsLabel = new JLabel();
        affectsVersionsLabel.setFont(new Font(affectsVersionsLabel.getFont().getName(), affectsVersionsLabel.getFont().getStyle(), 11));
        affectsVersionsLabel.setText("<html>Affects<br>  Versions:</html>");
        panel1.add(affectsVersionsLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(66, 28), null, 0, false));
        fixForScrollPane = new JScrollPane();
        panel1.add(fixForScrollPane, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fixForList = new JList();
        fixForList.setVisibleRowCount(5);
        fixForScrollPane.setViewportView(fixForList);
        componentsScrollPane = new JScrollPane();
        panel1.add(componentsScrollPane, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        componentsList = new JList();
        componentsList.setVisible(true);
        componentsList.setVisibleRowCount(5);
        componentsScrollPane.setViewportView(componentsList);
        affectVersionScrollPane = new JScrollPane();
        panel1.add(affectVersionScrollPane, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        affectsVersionsList = new JList();
        affectsVersionsList.setVisibleRowCount(5);
        affectVersionScrollPane.setViewportView(affectsVersionsList);
        final JLabel label2 = new JLabel();
        label2.setText("Issue Attributes");
        rootPanel.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel2, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        reporterLabel = new JLabel();
        reporterLabel.setFont(new Font(reporterLabel.getFont().getName(), reporterLabel.getFont().getStyle(), 11));
        reporterLabel.setHorizontalAlignment(2);
        reporterLabel.setText("Reporter:");
        panel2.add(reporterLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(73, 14), null, 0, false));
        reporterComboBox = new JComboBox();
        reporterComboBox.setLightWeightPopupEnabled(false);
        reporterComboBox.setMaximumRowCount(5);
        panel2.add(reporterComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(160, 27), null, 0, false));
        assigneeLabel = new JLabel();
        assigneeLabel.setFont(new Font(assigneeLabel.getFont().getName(), assigneeLabel.getFont().getStyle(), 11));
        assigneeLabel.setHorizontalAlignment(2);
        assigneeLabel.setText("Assignee:");
        panel2.add(assigneeLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(73, 14), null, 0, false));
        assigneeComboBox = new JComboBox();
        assigneeComboBox.setLightWeightPopupEnabled(false);
        assigneeComboBox.setMaximumRowCount(5);
        panel2.add(assigneeComboBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(160, 27), null, 0, false));
        statusLabel = new JLabel();
        statusLabel.setFont(new Font(statusLabel.getFont().getName(), statusLabel.getFont().getStyle(), 11));
        statusLabel.setHorizontalAlignment(2);
        statusLabel.setRequestFocusEnabled(false);
        statusLabel.setText("Status:");
        panel2.add(statusLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(73, 14), null, 0, false));
        statusScrollPane = new JScrollPane();
        panel2.add(statusScrollPane, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        statusList = new JList();
        statusList.setVisibleRowCount(5);
        statusScrollPane.setViewportView(statusList);
        resolutionsLabel = new JLabel();
        resolutionsLabel.setFont(new Font(resolutionsLabel.getFont().getName(), resolutionsLabel.getFont().getStyle(), 11));
        resolutionsLabel.setHorizontalAlignment(2);
        resolutionsLabel.setText("Resolutions:");
        panel2.add(resolutionsLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(73, 14), null, 0, false));
        resolutionScrollPane = new JScrollPane();
        panel2.add(resolutionScrollPane, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        resolutionsList = new JList();
        resolutionsList.setVisibleRowCount(5);
        resolutionScrollPane.setViewportView(resolutionsList);
        prioritiesLabel = new JLabel();
        prioritiesLabel.setFont(new Font(prioritiesLabel.getFont().getName(), prioritiesLabel.getFont().getStyle(), 11));
        prioritiesLabel.setHorizontalAlignment(2);
        prioritiesLabel.setText("Priorities:");
        prioritiesLabel.setVerticalTextPosition(0);
        panel2.add(prioritiesLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(73, 14), null, 0, false));
        prioritiesScrollPane = new JScrollPane();
        panel2.add(prioritiesScrollPane, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        prioritiesList = new JList();
        prioritiesList.setVisibleRowCount(5);
        prioritiesScrollPane.setViewportView(prioritiesList);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        projectScrollPane = new JScrollPane();
        panel3.add(projectScrollPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 80), null, 0, false));
        projectList = new JList();
        projectList.setInheritsPopupMenu(false);
        projectList.setLayoutOrientation(0);
        projectList.setVisibleRowCount(5);
        projectScrollPane.setViewportView(projectList);
        final JLabel label3 = new JLabel();
        label3.setFont(new Font(label3.getFont().getName(), label3.getFont().getStyle(), 11));
        label3.setRequestFocusEnabled(true);
        label3.setText("Issue Type:");
        panel3.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(70, 14), null, 0, false));
        issueTypeScrollPane = new JScrollPane();
        issueTypeScrollPane.setDoubleBuffered(false);
        panel3.add(issueTypeScrollPane, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 80), null, 0, false));
        issueTypeList = new JList();
        issueTypeList.setVisibleRowCount(5);
        issueTypeScrollPane.setViewportView(issueTypeList);
        final JLabel label4 = new JLabel();
        label4.setFont(new Font(label4.getFont().getName(), label4.getFont().getStyle(), 11));
        label4.setText("Project:");
        label4.setVerticalAlignment(1);
        panel3.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(70, 14), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Project/ Issue");
        rootPanel.add(label5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fixForLabel.setLabelFor(fixForScrollPane);
        componentsLabel.setNextFocusableComponent(componentsScrollPane);
        componentsLabel.setLabelFor(componentsScrollPane);
        affectsVersionsLabel.setLabelFor(affectVersionScrollPane);
        reporterLabel.setLabelFor(reporterComboBox);
        assigneeLabel.setLabelFor(assigneeComboBox);
        statusLabel.setLabelFor(statusScrollPane);
        resolutionsLabel.setLabelFor(resolutionScrollPane);
        prioritiesLabel.setLabelFor(prioritiesScrollPane);
        label3.setLabelFor(issueTypeScrollPane);
        label4.setLabelFor(projectScrollPane);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

    // CHECKSTYLE:ON

    private final class FilterActionClear extends AbstractAction {
        private static final String CLEAR_FILTER = "Clear filter";

        private FilterActionClear() {
            putValue(Action.NAME, CLEAR_FILTER);
        }

        public void actionPerformed(ActionEvent event) {
            if (filterListModel != null) {
                initialFilter.clear();
                ApplicationManager.getApplication().executeOnPooledThread(new SyncViewWithModelRunnable());
            }
        }
    }

    private void enableProjectDependentLists(boolean value) {
        this.fixForList.setEnabled(value);
        this.componentsList.setEnabled(value);
        this.affectsVersionsList.setEnabled(value);

        if (this.fixForList.getModel().getSize() == 0) {
            this.fixForScrollPane.setEnabled(false);
        } else {
            this.fixForScrollPane.setEnabled(true);
        }
        if (this.componentsList.getModel().getSize() == 0) {
            this.componentsScrollPane.setEnabled(false);
        } else {
            this.componentsScrollPane.setEnabled(true);
        }
        if (this.affectsVersionsList.getModel().getSize() == 0) {
            this.affectVersionScrollPane.setEnabled(false);
        } else {
            this.affectVersionScrollPane.setEnabled(true);
        }

        rootPanel.validate();
    }

    private void clearProjectDependentLists() {
        this.issueTypeList.setListData(new Object[0]);
        this.fixForList.setListData(new Object[0]);
        this.componentsList.setListData(new Object[0]);
        this.affectsVersionsList.setListData(new Object[0]);

        enableProjectDependentLists(false);
    }

    private void refreshGlobalIssueTypeList() {
        enableFields(false);
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            public void run() {
                final ModalityState modalityState = ModalityState.stateForComponent(JiraIssuesFilterPanel.this.getRootPane());
                try {
                    final List<JIRAConstant> issueTypes = jiraServerModel.getIssueTypes(jiraServerCfg, null, true);
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        public void run() {
                            issueTypeList.setListData(issueTypes.toArray());
                            enableFields(true);
                        }
                    }, modalityState);
                } catch (JIRAException e) {
                    showErrorMessage(e, "Cannot retrieve issue types", modalityState);
                }
            }
        });
    }

    private void showErrorMessage(final JIRAException e, final String description, final ModalityState modalityState) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                if (JiraIssuesFilterPanel.this.getRootPane().isShowing()) {
                    DialogWithDetails.showExceptionDialog(JiraIssuesFilterPanel.this.getRootPane(),
                            description.length() > 0 ? description : "Cannot retrieve metadata from JIRA", e);
                }
            }
        }, modalityState);
    }

    private void enableFields(final boolean enable) {
        projectList.setEnabled(enable);
        issueTypeList.setEnabled(enable);
        fixForList.setEnabled(enable);
        affectsVersionsList.setEnabled(enable);
        reporterComboBox.setEnabled(enable);
        assigneeComboBox.setEnabled(enable);
        componentsList.setEnabled(enable);
        resolutionsList.setEnabled(enable);
        statusList.setEnabled(enable);
        prioritiesList.setEnabled(enable);
        getOKAction().setEnabled(enable);
        clearFilterAction.setEnabled(enable);
    }

    private void refreshProjectDependentLists() {
        if (currentJiraProject != null) {
            if (currentJiraProject.getId() == JIRAServerCache.ANY_ID) {
                clearProjectDependentLists();
            } else {
                setProjectDependendListValues();
            }
        } else {
            clearProjectDependentLists();
        }
    }

    private void setProjectDependendListValues() {
        enableFields(false);
        final ModalityState modality = ModalityState.stateForComponent(JiraIssuesFilterPanel.this.getRootPane());
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            public void run() {
                try {
                    final List<JIRAConstant> issueType = jiraServerModel.getIssueTypes(jiraServerCfg, currentJiraProject, true);
                    final List<JIRAFixForVersionBean> fixForVersion = jiraServerModel.getFixForVersions(jiraServerCfg,
                            currentJiraProject, true);

                    final List<JIRAComponentBean> finalComponents = jiraServerModel
                            .getComponents(jiraServerCfg, currentJiraProject, true);

                    final List<JIRAVersionBean> versions = jiraServerModel.getVersions(jiraServerCfg, currentJiraProject, true);
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        public void run() {
                            if (JiraIssuesFilterPanel.this.isShowing()) {
                                issueTypeList.setListData(issueType.toArray());
                                fixForList.setListData(fixForVersion.toArray());
                                componentsList.setListData(finalComponents.toArray());
                                affectsVersionsList.setListData(versions.toArray());
                                enableProjectDependentLists(true);
                                enableFields(true);
                            }
                        }
                    }, modality);
                } catch (JIRAException e) {
                    showErrorMessage(e, "", modality);
                }
            }
        });
    }

    public void setFilter(final List<JIRAQueryFragment> advancedQuery) {
        this.initialFilter = advancedQuery;
    }

    @Override
    public void show() {
        projectList.addListSelectionListener(null);
        enableFields(false);
        ApplicationManager.getApplication().executeOnPooledThread(new SyncViewWithModelRunnable());
        super.show();
    }

    public void setListValues(JList list, List<Integer> selection) {
        int i = 0;
        int[] sel = new int[selection.size()];
        for (Integer integer : selection) {
            sel[i++] = integer;
        }
        list.setSelectedIndices(sel);
        list.ensureIndexIsVisible(selection.size() > 0 ? selection.iterator().next() : 0);
    }

    public List<Integer> getSelection(@NotNull final ListModel model, List<JIRAQueryFragment> advancedQuery) {
        if (advancedQuery == null) {
            return Collections.emptyList();
        }

        List<Integer> selection = new ArrayList<Integer>();
        for (int i = 0, size = model.getSize(); i < size; ++i) {
            for (JIRAQueryFragment jiraQueryFragment : advancedQuery) {
                JIRAQueryFragment fragment = (JIRAQueryFragment) model.getElementAt(i);
                if (jiraQueryFragment.getQueryStringFragment().equals(fragment.getQueryStringFragment())) {
                    selection.add(i);
                }
            }
        }
        return selection;
    }

    public void setComboValue(JComboBox combo, List<JIRAQueryFragment> advancedQuery) {

        combo.setSelectedIndex(-1);

        for (int i = 0, size = combo.getModel().getSize(); i < size; ++i) {
            for (JIRAQueryFragment jiraQueryFragment : advancedQuery) {
                JIRAQueryFragment fragment = (JIRAQueryFragment) combo.getModel().getElementAt(i);
                if (jiraQueryFragment.getQueryStringFragment().equals(fragment.getQueryStringFragment())) {
                    combo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    public List<JIRAQueryFragment> getFilter() {
        List<JIRAQueryFragment> query = new ArrayList<JIRAQueryFragment>();
        for (Object o : projectList.getSelectedValues()) {
            query.add((JIRAQueryFragment) o);
        }
        for (Object o : issueTypeList.getSelectedValues()) {
            query.add((JIRAQueryFragment) o);
        }
        for (Object o : statusList.getSelectedValues()) {
            query.add((JIRAQueryFragment) o);
        }
        for (Object o : prioritiesList.getSelectedValues()) {
            query.add((JIRAQueryFragment) o);
        }
        for (Object o : resolutionsList.getSelectedValues()) {
            query.add((JIRAQueryFragment) o);
        }
        for (Object o : fixForList.getSelectedValues()) {
            query.add((JIRAQueryFragment) o);
        }
        for (Object o : componentsList.getSelectedValues()) {
            query.add((JIRAQueryFragment) o);
        }
        for (Object o : affectsVersionsList.getSelectedValues()) {
            query.add((JIRAQueryFragment) o);
        }

        if (assigneeComboBox.getSelectedItem() != null) {
            query.add((JIRAQueryFragment) assigneeComboBox.getSelectedItem());
        }

        if (reporterComboBox.getSelectedItem() != null) {
            query.add((JIRAQueryFragment) reporterComboBox.getSelectedItem());
        }

        return query;
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return this.$$$getRootComponent$$$();
    }

    @Override
    public synchronized void doCancelAction() {
        super.doCancelAction();
        this.windowClosed = true;
    }

    private synchronized boolean isWindowClosed() {
        return windowClosed;
    }

    private class SyncViewWithModelRunnable implements Runnable {
        public void run() {
            final ModalityState modality = ModalityState.stateForComponent(JiraIssuesFilterPanel.this.getRootPane());


            try {
                projectList.setListData(jiraServerModel.getProjects(jiraServerCfg).toArray());
                final List<Integer> prjSel = getSelection(projectList.getModel(), initialFilter);
                if (prjSel.size() == 1) {
                    currentJiraProject = (JIRAProject) projectList.getModel().getElementAt(prjSel.get(0));
                } else {
                    currentJiraProject = null;
                }

                issueTypeList.setListData(jiraServerModel.getIssueTypes(jiraServerCfg, currentJiraProject, true).toArray());
                statusList.setListData(jiraServerModel.getStatuses(jiraServerCfg).toArray());
                prioritiesList.setListData(jiraServerModel.getPriorities(jiraServerCfg, true).toArray());
                resolutionsList.setListData(jiraServerModel.getResolutions(jiraServerCfg, true).toArray());
                fixForList.setListData(jiraServerModel.getFixForVersions(jiraServerCfg, currentJiraProject, true).toArray());

                JiraIssuesFilterPanel.this.componentsList.setListData(
                        jiraServerModel.getComponents(jiraServerCfg, currentJiraProject, false).toArray());

                affectsVersionsList.setListData(jiraServerModel.getVersions(jiraServerCfg, currentJiraProject, true).toArray());

                reporterComboBox.removeAllItems();
                reporterComboBox.addItem(new JIRAReporterBean(JIRAServerCache.ANY_ID, "Any User", null));
                reporterComboBox.addItem(new JIRAReporterBean((long) -1, "Current User", jiraServerCfg.getUserName()));

                assigneeComboBox.removeAllItems();
                assigneeComboBox.addItem(new JIRAAssigneeBean(JIRAServerCache.ANY_ID, "Any User", ""));
                assigneeComboBox.addItem(new JIRAAssigneeBean((long) -1, "Unassigned", "unassigned"));
                assigneeComboBox.addItem(new JIRAAssigneeBean((long) -1, "Current User", jiraServerCfg.getUserName()));


                if (!isWindowClosed()) {
                    enableFields(false);
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        public void run() {
                            setListValues(projectList, prjSel);
                            setListValues(statusList, getSelection(statusList.getModel(), initialFilter));
                            setListValues(prioritiesList, getSelection(prioritiesList.getModel(), initialFilter));
                            setListValues(resolutionsList, getSelection(resolutionsList.getModel(), initialFilter));
                            setListValues(issueTypeList, getSelection(issueTypeList.getModel(), initialFilter));
                            setListValues(JiraIssuesFilterPanel.this.componentsList,
                                    getSelection(JiraIssuesFilterPanel.this.componentsList.getModel(), initialFilter));
                            setListValues(fixForList, getSelection(fixForList.getModel(), initialFilter));
                            setListValues(affectsVersionsList, getSelection(affectsVersionsList.getModel(), initialFilter));
                            setComboValue(assigneeComboBox, initialFilter);
                            setComboValue(reporterComboBox, initialFilter);
                            addProjectActionListener();
                            enableFields(true);
                        }
                    }, modality);
                }

            } catch (JIRAException e) {
                final JIRAException exception = e;

                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    public void run() {
                        DialogWithDetails.showExceptionDialog(JiraIssuesFilterPanel.this.getContentPane(),
                                "Cannot retrieve metadata from JIRA", exception);
                    }
                }, modality);
            }
        }
    }
}



