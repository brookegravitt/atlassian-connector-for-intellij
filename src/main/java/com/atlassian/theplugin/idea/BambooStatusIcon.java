package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BambooDisplayComponent;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 3:54:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooStatusIcon extends JLabel implements BambooDisplayComponent {

    public void updateBambooStatus(String statusIcon, String fullInfo) {
        setText(statusIcon);
        setToolTipText(fullInfo);
    }
}
