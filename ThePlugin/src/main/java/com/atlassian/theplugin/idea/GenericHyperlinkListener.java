package com.atlassian.theplugin.idea;

import com.intellij.ide.BrowserUtil;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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
