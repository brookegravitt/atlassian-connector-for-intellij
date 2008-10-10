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

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooBuildAdapter;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class BambooBuildAdapterIdea extends BambooBuildAdapter {
	private static final Icon ICON_RED = IconLoader.getIcon("/icons/icn_plan_failed.gif");
	private static final Icon ICON_GREEN = IconLoader.getIcon("/icons/icn_plan_passed.gif");
	private static final Icon ICON_GREY = IconLoader.getIcon("/icons/icn_plan_disabled.gif");

	private static final Icon ICON_MY_BUILD_RED = IconLoader.getIcon("/actions/lightning.png");
	private static final Icon ICON_MY_BUILD_GREEN = IconLoader.getIcon("/icons/lightning_green.png");


	public Icon getMyBuildIcon() {
		if (getState() == BuildState.FAIL && build.isMyBuild()) {
			return ICON_MY_BUILD_RED;
		} else if (getState() == BuildState.PASS && build.isMyBuild()) {
			return ICON_MY_BUILD_GREEN;
		} else {
			return null;
		}
	}

	public enum BuildState {
		PASS, FAIL, UNKNOWN 
	}

	public BuildState getState() {
		if (build.getEnabled()) {
			switch (build.getStatus()) {
				case BUILD_FAILED:
					return BuildState.FAIL;
				case BUILD_SUCCEED:
					return BuildState.PASS;
				case UNKNOWN:
				default:
					return BuildState.UNKNOWN;
			}
		} else {
			return BuildState.UNKNOWN;
		}
	}

	public BambooBuildAdapterIdea(BambooBuild build) {
		super(build);
	}

	public Icon getBuildIcon() {
		final BuildState buildState = getState();
		switch (buildState) {
			case FAIL:
				return ICON_RED;
			case PASS:
				return ICON_GREEN;
			case UNKNOWN:
			default:
				return ICON_GREY;
		}
	}

}
