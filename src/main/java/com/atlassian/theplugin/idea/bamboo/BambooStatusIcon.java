package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.ServerType;
import com.atlassian.theplugin.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.StatusBarPluginIcon;
import com.atlassian.theplugin.idea.PluginToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BambooStatusIcon extends StatusBarPluginIcon implements BambooStatusDisplay {

	private static final Icon ICON_RED = IconLoader.getIcon("/icons/red-16.png");
	private static final Icon ICON_GREEN = IconLoader.getIcon("/icons/green-16.png");
	private static final Icon ICON_GREY = IconLoader.getIcon("/icons/grey-16.png");

//	private final PluginStatusBarToolTip tooltip;

	/**
	 * @param project reference to the project that owns this icon.
	 */
    public BambooStatusIcon(final Project project) {

		super(project);

		// show tooltip on mouse over
//		tooltip = new PluginStatusBarToolTip(WindowManager.getInstance().getFrame(aProjectComponent.getProject()));

		addMouseListener(new MouseAdapter() {
            /*
            public void mouseEntered(MouseEvent e) {

				tooltip.showToltip(
						(int) MouseInfo.getPointerInfo().getLocation().getX(),
						(int) MouseInfo.getPointerInfo().getLocation().getY());
			}
			*/

			// show/hide toolbar on click
			public void mouseClicked(MouseEvent e) {
				PluginToolWindow.focusPanel(project, PluginToolWindow.ToolWindowPanels.BAMBOO);
            }
		});

	}


	public void updateBambooStatus(BuildStatus status, String fullInfo) {
//		tooltip.setHtmlContent(fullInfo);
		switch (status) {
			case BUILD_FAILED:
                setToolTipText("Some builds failed. Click to see details.");
				setIcon(ICON_RED);
                break;
			case UNKNOWN:
				setIcon(ICON_GREY);
				break;
			case BUILD_SUCCEED:
                setToolTipText("All builds currently passing.");
				setIcon(ICON_GREEN);
				break;
			default:
				throw new IllegalArgumentException("Illegal state of build.");
		}
	}

	public void showOrHideIcon() {
		super.showOrHideIcon(ServerType.BAMBOO_SERVER);
	}

}
