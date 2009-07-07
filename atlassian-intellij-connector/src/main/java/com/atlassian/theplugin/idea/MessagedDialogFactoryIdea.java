package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.commons.ssl.CertMessageDialog;
import com.atlassian.theplugin.commons.ssl.MessageDialogFactory;
import com.atlassian.theplugin.idea.ui.CertMessageDialogImpl;
import com.intellij.openapi.project.Project;

/**
 * Created by IntelliJ IDEA.
 * User: pmaruszak
 * Date: Jul 7, 2009
 */
public class MessagedDialogFactoryIdea implements MessageDialogFactory {
    private Project project;

    public CertMessageDialog getMessageDialog() {
        if (project != null) {
            return new CertMessageDialogImpl();
        }
        return null;
    }

    void setProject(Project project) {
        this.project = project;
    }
}
