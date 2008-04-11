package com.atlassian.theplugin.idea;

import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.ui.HyperlinkLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-04-11
 * Time: 16:39:01
 * To change this template use File | Settings | File Templates.
 */
public class ToolWindowConfigPanel extends JPanel {
	public ToolWindowConfigPanel() {

		super(new BorderLayout());

		HyperlinkLabel link = new HyperlinkLabel("Configure Plugin");
		link.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				ShowSettingsUtil.getInstance().
						editConfigurable(IdeaHelper.getCurrentProject(), IdeaHelper.getAppComponent());
			}
		});

		this.add(link, BorderLayout.CENTER);
	}
}
