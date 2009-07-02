package com.atlassian.theplugin.idea;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.util.IconLoader;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

/**
 * User: jgorycki
 * Date: Jan 29, 2009
 * Time: 4:56:19 PM
 */
public class AboutForm {
    private JPanel mainPanel;
    private JEditorPane aboutText;
    private JLabel iconLabel;

    public AboutForm() {
        iconLabel.setIcon(IconLoader.getIcon("/icons/Atlassian.png"));
        iconLabel.setText("");

        aboutText.setContentType("text/html");
        aboutText.setEditable(false);
        aboutText.setOpaque(true);
        aboutText.setBackground(Color.WHITE);
        aboutText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        aboutText.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    BrowserUtil.launchBrowser(e.getURL().toString());
                }
            }
        });
        setAboutText();
    }

    private void setAboutText() {
        aboutText.setText("<html><body><center>" + "The Atlassian Connector for IntelliJ IDEA is an IDEA plugin that lets you " + "work with the Atlassian products within your IDE. Now you don't "
                + "have to switch between websites, email messages and news feeds to " + "see what's happening to your project and your code. Instead, you "
                + "can see the relevant <a href=\"http://www.atlassian.com/software/jira\">JIRA</a> issues, " + "<a href=\"http://www.atlassian.com/software/crucible\">Crucible</a> reviews "
                + "and <a href=\"http://www.atlassian.com/software/bamboo\">Bamboo</a> build " + "information right there in your development environment. Viewing your "
                + "code in <a href=\"http://www.atlassian.com/software/fisheye\">FishEye</a> is just a click away." + "<br><br><br><br>" + "<font size=\"5\">Developed by Atlassian for you to lust after<br>"
                + "<a href=\"http://www.atlassian.com/\"><b>http://www.atlassian.com/<b></a></font>" + "<br><br><br><br>Licensed under the Apache License, Version 2.2. Copyright (c) Atlassian 2009"
                + "</center></body></html>");
    }

    public JPanel getRootPane() {
        return mainPanel;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new FormLayout("fill:d:grow", "center:max(d;4px):noGrow,top:3dlu:noGrow,center:d:grow"));
        mainPanel.setBackground(new Color(-1));
        mainPanel.setMaximumSize(new Dimension(600, 500));
        mainPanel.setMinimumSize(new Dimension(600, 500));
        mainPanel.setPreferredSize(new Dimension(600, 500));
        aboutText = new JEditorPane();
        aboutText.setMaximumSize(new Dimension(500, 250));
        aboutText.setMinimumSize(new Dimension(500, 250));
        aboutText.setPreferredSize(new Dimension(500, 250));
        CellConstraints cc = new CellConstraints();
        mainPanel.add(aboutText, cc.xy(1, 3, CellConstraints.CENTER, CellConstraints.FILL));
        iconLabel = new JLabel();
        iconLabel.setMaximumSize(new Dimension(500, 216));
        iconLabel.setMinimumSize(new Dimension(500, 216));
        iconLabel.setPreferredSize(new Dimension(500, 216));
        iconLabel.setRequestFocusEnabled(false);
        iconLabel.setText("Label");
        mainPanel.add(iconLabel, cc.xy(1, 1, CellConstraints.CENTER, CellConstraints.TOP));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }
}
