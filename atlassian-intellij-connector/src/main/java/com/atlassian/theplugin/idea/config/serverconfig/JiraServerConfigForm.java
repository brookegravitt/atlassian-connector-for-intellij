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

package com.atlassian.theplugin.idea.config.serverconfig;

import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.jira.JIRAServerFacade;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Plugin configuration form.
 */
public class JiraServerConfigForm {

    private JPanel rootComponent;

    private transient GenericServerConfigForm genericServerConfigForm;
    private JCheckBox cbUseBasicAuthentication;
    private final Project project;
    private final UserCfg defaultUser;
    private final transient JIRAServerFacade jiraServerFacade;

    public JiraServerCfg getJiraServerCfg() {
        return jiraServerCfg;
    }

    private JiraServerCfg jiraServerCfg;

    public JiraServerConfigForm(Project project, final UserCfg defaultUser, JIRAServerFacade jiraServerFacade) {
        this.project = project;
        this.defaultUser = defaultUser;
        this.jiraServerFacade = jiraServerFacade;
    }

    public void setData(@NotNull final JiraServerCfg serverCfg) {
        jiraServerCfg = serverCfg;
        cbUseBasicAuthentication.setSelected(serverCfg.isDontUseBasicAuth());
        genericServerConfigForm.setData(serverCfg);
    }

    public void finalizeData() {
        genericServerConfigForm.finalizeData();
    }

    public void saveData() {
        genericServerConfigForm.saveData();
        if (jiraServerCfg != null) {
            jiraServerCfg.setDontUseBasicAuth(cbUseBasicAuthentication.isSelected());
        }
    }

    public JComponent getRootComponent() {
        return rootComponent;
    }

    public void setVisible(boolean visible) {
        rootComponent.setVisible(visible);
    }

    private void createUIComponents() {
        genericServerConfigForm =
                new GenericServerConfigForm(project, defaultUser, new ProductConnector(jiraServerFacade));
        cbUseBasicAuthentication = new JCheckBox("Do not use HTTP authentication");
    }

    // CHECKSTYLE:OFF

    // CHECKSTYLE:ON

    // for use by unit test only

    public GenericServerConfigForm getGenericServerConfigForm() {
        return genericServerConfigForm;
    }

}