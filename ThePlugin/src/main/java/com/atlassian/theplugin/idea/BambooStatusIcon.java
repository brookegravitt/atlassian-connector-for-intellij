package com.atlassian.theplugin.idea;

import com.atlassian.theplugin.bamboo.BuildStatus;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class BambooStatusIcon extends JLabel {

    private static Icon iconRed;
    private static Icon iconGreen;
    private static Icon iconGrey;

	private Project project;


	static {
        iconRed = IconLoader.getIcon("/icons/red-16.png");
        iconGreen = IconLoader.getIcon("/icons/green-16.png");
        iconGrey = IconLoader.getIcon("/icons/grey-16.png");
    }

   private PluginStatusBarToolTip tooltip;

    private static final int X_OFFSET = 160;
    private static final int Y_OFFSET = 30;

	/**
	 *
	 * @param aProject reference to the project 
	 */
	BambooStatusIcon(Project aProject) {


		// show tooltip on mouse over
        tooltip = new PluginStatusBarToolTip(WindowManager.getInstance().getFrame(aProject));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {

                Window win = SwingUtilities.getWindowAncestor(BambooStatusIcon.this);


                tooltip.showToltip(
                        win.getX() + win.getWidth()
                                - BambooStatusIcon.this.getX() - X_OFFSET, win.getY() + win.getHeight() - Y_OFFSET);
            }
        });

    }

    public void updateBambooStatus(BuildStatus status, String fullInfo) {

        tooltip.setHtmlContent(fullInfo);

        switch (status) {
            case BUILD_FAILED:
                setIcon(iconRed);
                break;
            case UNKNOWN:
                setIcon(iconGrey);
                break;
            case BUILD_SUCCEED:
                setIcon(iconGreen);
                break;
            default:
                throw new IllegalArgumentException("Illegal state of build.");
        }
    }

}
