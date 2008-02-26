package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.bamboo.BuildStatus;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.ThePluginProjectComponent;
import com.atlassian.theplugin.StatusBarPluginIcon;
import com.atlassian.theplugin.ServerType;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BambooStatusIcon extends StatusBarPluginIcon implements BambooStatusDisplay {

	private static final Icon ICON_RED = IconLoader.getIcon("/icons/icn_plan_failed.gif");
	private static final Icon ICON_GREEN = IconLoader.getIcon("/icons/icn_plan_passed.gif");
	private static final Icon ICON_GREY = IconLoader.getIcon("/icons/icn_plan_disabled.gif");

//	private final PluginStatusBarToolTip tooltip;

	/**
	 * @param aProjectComponent reference to the project component
	 */
    public BambooStatusIcon(final ThePluginProjectComponent aProjectComponent) {

		super(aProjectComponent.getProject());

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
                IdeaHelper.focusPanel(IdeaHelper.TOOLWINDOW_PANEL_BAMBOO);
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
