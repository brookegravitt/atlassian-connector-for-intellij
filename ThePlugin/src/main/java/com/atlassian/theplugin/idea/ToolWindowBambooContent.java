package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.ProjectManager;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import static javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;
import javax.swing.event.HyperlinkListener;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-31
 * Time: 17:36:10
 * To change this template use File | Settings | File Templates.
 */
public class ToolWindowBambooContent extends JEditorPane implements BambooStatusDisplay {

	ToolWindowBambooContent() {
		setEditable(false);
        setContentType("text/html");
		addHyperlinkListener(new GenericHyperlinkListener());
	}

    public void updateBambooStatus(BuildStatus generalBuildStatus, String htmlPage) {
		this.setText(htmlPage);
	}
	
}
