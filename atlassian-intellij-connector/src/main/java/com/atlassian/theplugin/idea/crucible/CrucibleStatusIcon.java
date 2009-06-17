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

package com.atlassian.theplugin.idea.crucible;

import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.idea.PluginToolWindow;
import com.atlassian.theplugin.idea.StatusBarPluginIcon;
import com.atlassian.theplugin.idea.config.ProjectCfgManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Defines crucible icon behaviour.
 * Created by IntelliJ IDEA.
 * User: Jacek
 * Date: 2008-02-15
 * Time: 10:57:34
 * To change this template use File | Settings | File Templates.
 */
public class CrucibleStatusIcon extends StatusBarPluginIcon {

	private static final Icon ICON_NEW = IconLoader.getIcon("/icons/ico_crucible_green.png");
	private static final Icon ICON_STANDARD = IconLoader.getIcon("/icons/ico_crucible_grey.png");
	private static final Icon ICON_ERROR = IconLoader.getIcon("/icons/ico_crucible_red.png");
	private static final String NO_NEW_REVIEWS = "No new reviews and review changes.";
	private static final String NEW_REVIEWS = "New Crucible events are available. Click for details.";
	private static final String ERROR_REVIEWS = "Some errors occured. Check connections to Crucible servers.";

	public CrucibleStatusIcon(final Project project, ProjectCfgManagerImpl cfgManager,
			@NotNull final PluginToolWindow pluginToolWindow) {
		super(project, cfgManager);
		resetIcon();

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				pluginToolWindow.focusPanel(PluginToolWindow.ToolWindowPanels.CRUCIBLE);
				resetIcon();
			}
		});
	}

	/**
	 * Sets the icon to NEW REVEWS state (blue icon, number of revious in text label, tooltip text)
	 *
	 * @param numOfNewReviews number of new reviews
	 */
	public void triggerNewReviewAction(int numOfNewReviews, boolean exceptionRaised) {
		if (!exceptionRaised) {
			this.setIcon(ICON_NEW);
			this.setToolTipText(NEW_REVIEWS);
		} else {
			this.setIcon(ICON_ERROR);
			this.setToolTipText(ERROR_REVIEWS);
		}

		this.setText(Integer.toString(numOfNewReviews));
	}

	/**
	 * Sets the icon to standard state (sets grey icon, removes text label, change tooltip)
	 */
	public void resetIcon() {
		this.setIcon(ICON_STANDARD);
		this.setToolTipText(NO_NEW_REVIEWS);
		this.setText(null);
	}

	public void showOrHideIcon() {
		super.showOrHideIcon(ServerType.CRUCIBLE_SERVER);
	}
}
