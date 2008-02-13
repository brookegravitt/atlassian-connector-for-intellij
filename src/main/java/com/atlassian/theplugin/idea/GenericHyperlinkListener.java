package com.atlassian.theplugin.idea;

import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ide.BrowserUtil;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;

/**
 * A generic HyperlinkListener which catches plugin configuration URLs and opens the settings panel.
 * In all other cases, a browser is opened with the URL clicked.
*/
public class GenericHyperlinkListener implements HyperlinkListener {
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType().equals(javax.swing.event.HyperlinkEvent.EventType.ACTIVATED)) {
            BrowserUtil.launchBrowser(e.getURL().toString());
        }
    }
}
