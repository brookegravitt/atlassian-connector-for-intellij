package com.atlassian.theplugin.idea.config.serverconfig.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.atlassian.theplugin.commons.ServerType;

/**
 * User: kalamon
 * Date: Jul 8, 2009
 * Time: 2:11:41 PM
 */
public class AddStudioServerSetAction extends AbstractServerAction {
    public void actionPerformed(AnActionEvent event) {
        addServer(event, ServerType.JIRA_STUDIO_SERVER);
    }
}
