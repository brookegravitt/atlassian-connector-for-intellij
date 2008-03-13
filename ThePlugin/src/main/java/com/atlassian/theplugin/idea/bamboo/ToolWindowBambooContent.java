package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;

import javax.swing.*;

public class ToolWindowBambooContent extends JEditorPane implements BambooStatusDisplay {
	public ToolWindowBambooContent() {
		setEditable(false);
        setContentType("text/html");
        addHyperlinkListener(new GenericHyperlinkListener());
	}
    
    public void updateBambooStatus(BuildStatus generalBuildStatus, String htmlPage) {
        this.setText(htmlPage);
        this.setCaretPosition(0);
    }
}
