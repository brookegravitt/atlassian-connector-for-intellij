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
package com.atlassian.theplugin.idea.jira.tree;

import com.atlassian.connector.commons.jira.beans.JIRAProject;
import com.atlassian.connector.commons.jira.beans.JIRAProjectBean;
import com.atlassian.connector.commons.jira.rss.JIRAException;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ui.DialogWithDetails;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ListSpeedSearch;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * @autrhor pmaruszak
 * @date Mar 24, 2010
 */
public class SelectJiraProjectDialog extends DialogWrapper {
    private JPanel panel = new JPanel (new BorderLayout());
    private JList list;
    private ListSpeedSearch speedList;
    private DefaultListModel listModel = new DefaultListModel();
    private final Project project;
    private final JiraServerData jiraServer;

    public SelectJiraProjectDialog(Project project, JiraServerData jiraServer) {
        super(project);
        this.project = project;
        this.jiraServer = jiraServer;
        list = new JList(listModel);
        speedList = new ListSpeedSearch(list);
        list.setModel(listModel);

        list.setVisibleRowCount(20);
        JScrollPane listScroller = new JScrollPane(speedList.getComponent());
        listScroller.setPreferredSize(new Dimension(380, 320));
        listScroller.setMinimumSize(new Dimension(350, 320));
        setModal(true);
        panel.add(listScroller, BorderLayout.CENTER);
        updateModel();
        setTitle("Select Project");
        init();

    }

    @Override
    protected JComponent createCenterPanel() {
        return panel;
    }

    @Nullable
    public JIRAProjectBean getSelectedProject() {
        return list.getSelectedValue() != null ? ((JiraProjectWrapper)list.getSelectedValue()).getJiraProject() : null;
    }
    private void updateModel()  {

        try {
            for (JIRAProject p : IdeaHelper.getJIRAServerModel(project).getProjects(jiraServer)) {
                listModel.addElement(new JiraProjectWrapper((JIRAProjectBean) p));
            }
        } catch (JIRAException e) {
            DialogWithDetails.showExceptionDialog(project, "Cannot retrieve project from server", "");
        }

    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    private class JiraProjectWrapper {
        private final JIRAProjectBean jiraProject;

        private JiraProjectWrapper(JIRAProjectBean jiraProject) {
            this.jiraProject = jiraProject;
        }

        public JIRAProjectBean getJiraProject() {
            return jiraProject;
        }

        @Override
        public String toString() {
            return jiraProject.getName() + " (" + jiraProject.getKey() + ")";
        }
    }
}
