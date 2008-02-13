package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.PluginStatusBarToolTip;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BambooStatusIcon extends JLabel implements BambooStatusDisplay {

	private static Icon iconRed;
	private static Icon iconGreen;
	private static Icon iconGrey;

	static {
		iconRed = IconLoader.getIcon("/icons/red-16.png");
		iconGreen = IconLoader.getIcon("/icons/green-16.png");
		iconGrey = IconLoader.getIcon("/icons/grey-16.png");
	}

	private PluginStatusBarToolTip tooltip;

	/**
	 * @param aProjectComponent reference to the project component
	 */
    public BambooStatusIcon(final ThePluginProjectComponent aProjectComponent) {

		// show tooltip on mouse over
		tooltip = new PluginStatusBarToolTip(WindowManager.getInstance().getFrame(aProjectComponent.getProject()));

		addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {

				tooltip.showToltip(
						(int) MouseInfo.getPointerInfo().getLocation().getX(),
						(int) MouseInfo.getPointerInfo().getLocation().getY());
			}

			// show/hide toolbar on click
			public void mouseClicked(MouseEvent e) {
				ToolWindow toolWindow = aProjectComponent.getToolWindow();
				toolWindow.activate(null);
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
