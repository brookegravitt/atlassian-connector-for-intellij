package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.exception.ThePluginException;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ide.BrowserUtil;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import static javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.*;
import java.io.IOException;


public class BambooStatusIcon extends JLabel {

    Icon iconRed, iconGreen, iconGrey;

    PluginStatusBarToolTip tooltip;

    
    BambooStatusIcon() {

        // load icons
        iconRed = IconLoader.getIcon("/icons/red-16.png");
        iconGreen = IconLoader.getIcon("/icons/green-16.png");
        iconGrey = IconLoader.getIcon("/icons/grey-16.png");
        
        // show tooltip on mouse over
		tooltip = new PluginStatusBarToolTip();

		addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {

			Window win = SwingUtilities.getWindowAncestor(BambooStatusIcon.this);



				tooltip.showToltip(win.getX() + win.getWidth() - BambooStatusIcon.this.getX() - 160, win.getY() + win.getHeight() - 30);
            }
        });

    }

    public void updateBambooStatus(BuildStatus status, String fullInfo) {

        tooltip.setHtmlContent(fullInfo);

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
            default:
                setIcon(iconGrey);
        }
    }

}
