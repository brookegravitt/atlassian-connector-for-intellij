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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class BambooBuildAdapterIdea extends BambooBuildAdapter {
	private static final Icon ICON_RED = IconLoader.getIcon("/icons/icn_plan_failed.gif");
	private static final Icon ICON_GREEN = IconLoader.getIcon("/icons/icn_plan_passed.gif");
	private static final Icon ICON_GREY = IconLoader.getIcon("/icons/icn_plan_disabled.gif");

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

}
