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

import com.atlassian.theplugin.commons.bamboo.AdjustedBuildStatus;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ui.AsyncProcessIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class BambooBuildAdapterIdea extends BambooBuildAdapter {
	private static final Icon ICON_RED = IconLoader.getIcon("/icons/icn_plan_failed.gif");
	private static final Icon ICON_GREEN = IconLoader.getIcon("/icons/icn_plan_passed.gif");
	private static final Icon ICON_GREY = IconLoader.getIcon("/icons/icn_plan_disabled.gif");

	private static final Icon ICON_IS_IN_QUEUE = IconLoader.getIcon("/icons/bamboo_building.gif");
	private static final Icon ICON_IS_BUILDING = IconLoader.getIcon("/icons/cup.png");

	private static final Icon ICON_MY_BUILD_RED = IconLoader.getIcon("/actions/lightning.png");
	private static final Icon ICON_MY_BUILD_GREEN = IconLoader.getIcon("/icons/lightning_green.png");

	public BambooBuildAdapterIdea(BambooBuild build) {
		super(build);

	}

	@Nullable
	public Icon getMyBuildIcon() {
		if (getStatus() == BuildStatus.FAILURE && build.isMyBuild()) {
			return ICON_MY_BUILD_RED;
		} else if (getStatus() == BuildStatus.SUCCESS && build.isMyBuild()) {
			return ICON_MY_BUILD_GREEN;
		} else {
			return null;
		}
	}

	@NotNull
	public Icon getIcon() {
		if (build.getEnabled()) {

			if (build.isBuilding()) {
				return ICON_IS_BUILDING;
			} else if (build.isInQueue()) {
				return ICON_IS_IN_QUEUE;
			}

			switch (getStatus()) {
				case FAILURE:
					return ICON_RED;
				case SUCCESS:
					return ICON_GREEN;
				case UNKNOWN:
					return ICON_GREY;
				default:
					break;
			}
		}
		return ICON_GREY;
	}

	@NotNull
	public AdjustedBuildStatus getAdjustedStatus() {
		if (build.getEnabled()) {
			switch (build.getStatus()) {
				case FAILURE:
					return AdjustedBuildStatus.FAILURE;
				case SUCCESS:
					return AdjustedBuildStatus.SUCCESS;
				case UNKNOWN:
					return AdjustedBuildStatus.UNKNOWN;
				default:
					break;
			}
		}
		return AdjustedBuildStatus.DISABLED;
	}

	public boolean areActionsAllowed() {
		final AdjustedBuildStatus buildStatus = getAdjustedStatus();
		return buildStatus != AdjustedBuildStatus.UNKNOWN && buildStatus != AdjustedBuildStatus.DISABLED;
	}

	public class BuildInProgressIcon extends AsyncProcessIcon {

		private Icon[] icons;
		private Icon passiveIcon;
		private static final int CYCLE_LENGTH = 600; // whole animation single cycle lenght
		private static final int CYCLE_GAP = 60; // break after every single cycle (best 'cycleLenght / number of frames')

		public BuildInProgressIcon(@org.jetbrains.annotations.NonNls String name) {
			super(name);

			// comment that line if you want to use standard IDEA small progress circle
//			initCustomLook();
		}

		private void initCustomLook() {
			loadIcons();
			init(icons, passiveIcon, CYCLE_LENGTH, CYCLE_GAP, -1);
		}

		private void loadIcons() {
			icons = new Icon[]{ICON_GREEN, ICON_GREY};

			passiveIcon = ICON_RED;
		}
	}
}
