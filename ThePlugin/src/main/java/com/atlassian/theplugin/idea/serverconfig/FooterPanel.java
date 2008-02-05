package com.atlassian.theplugin.idea.serverconfig;

import com.intellij.ide.BrowserUtil;
import com.intellij.ui.HyperlinkLabel;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: mwent
 * Date: 2008-02-05
 * Time: 11:42:01
 * To change this template use File | Settings | File Templates.
 */
public class FooterPanel extends JPanel {
	private HyperlinkLabel openJiraHyperlinkLabel;

	public FooterPanel() {
		initLayout();
    }

	private void initLayout() {
		BorderLayout gb = new BorderLayout();
		setLayout(gb);

		openJiraHyperlinkLabel = new HyperlinkLabel("Report a bug/issue/request.");
		openJiraHyperlinkLabel.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				BrowserUtil.launchBrowser("https://studio.atlassian.com/browse/PL");
			}
		});	
		add(openJiraHyperlinkLabel, BorderLayout.EAST);
	}
}
