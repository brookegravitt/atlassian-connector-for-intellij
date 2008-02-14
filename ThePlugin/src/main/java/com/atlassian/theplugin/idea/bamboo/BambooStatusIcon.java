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

	private static final Icon ICON_RED = IconLoader.getIcon("/icons/red-16.png");
	private static final Icon ICON_GREEN = IconLoader.getIcon("/icons/green-16.png");
	private static final Icon ICON_GREY = IconLoader.getIcon("/icons/grey-16.png");

	private final PluginStatusBarToolTip tooltip;

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
				setIcon(ICON_RED);
				break;
			case UNKNOWN:
				setIcon(ICON_GREY);
				break;
			case BUILD_SUCCEED:
				setIcon(ICON_GREEN);
				break;
			default:
				throw new IllegalArgumentException("Illegal state of build.");
		}
	}

}
