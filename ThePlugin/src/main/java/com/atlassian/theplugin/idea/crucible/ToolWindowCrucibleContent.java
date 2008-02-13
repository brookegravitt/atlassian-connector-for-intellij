package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.crucible.CrucibleStatusDisplay;
import com.atlassian.theplugin.idea.GenericHyperlinkListener;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-01-31
 * Time: 17:36:10
 * To change this template use File | Settings | File Templates.
 */
public class ToolWindowCrucibleContent extends JEditorPane implements CrucibleStatusDisplay {

	public ToolWindowCrucibleContent() {
		setEditable(false);
        setContentType("text/html");
		addHyperlinkListener(new GenericHyperlinkListener());
	}

    public void updateCrucibleStatus(String htmlPage) {
		this.setText(htmlPage);
	}
}