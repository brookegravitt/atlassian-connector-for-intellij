package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-31
 * Time: 17:36:10
 * To change this template use File | Settings | File Templates.
 */
public class ToolWindowBambooContent extends JEditorPane implements BambooStatusDisplay {
	public ToolWindowBambooContent() {
		setEditable(false);
        setContentType("text/html");
        addHyperlinkListener(new GenericHyperlinkListener());
	}
    
    public void updateBambooStatus(BuildStatus generalBuildStatus, String htmlPage) {
		this.setText(htmlPage);
	}
}
