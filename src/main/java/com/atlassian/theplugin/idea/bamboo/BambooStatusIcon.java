/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.idea.bamboo;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.commons.bamboo.BambooPopupInfo;
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


	public void updateBambooStatus(BuildStatus status, BambooPopupInfo notUsed) {
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
