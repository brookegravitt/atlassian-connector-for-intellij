package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BuildStatus;
import com.intellij.lang.properties.ResourceBundle;
import com.intellij.openapi.util.IconLoader;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Icon;

/**
 * Created by IntelliJ IDEA.
 * User: sginter
 * Date: Jan 15, 2008
 * Time: 3:54:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class BambooStatusIcon extends JLabel {

    Icon iconRed, iconGreen, iconGrey;

    BambooStatusIcon() {
        iconRed = IconLoader.getIcon("/icons/red-16.png");
        iconGreen = IconLoader.getIcon("/icons/green-16.png");
        iconGrey = IconLoader.getIcon("/icons/grey-16.png");

    }

    public void updateBambooStatus(BuildStatus status, String fullInfo) {
        setText("");
        setToolTipText(fullInfo);

        switch (status) {
            case ERROR:
                setIcon(iconGrey);
                break;
            case FAILED:
                setIcon(iconRed);
                break;
            case SUCCESS:
                setIcon(iconGreen);
                break;
        }
    }
}
