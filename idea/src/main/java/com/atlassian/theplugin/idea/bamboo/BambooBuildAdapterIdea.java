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

	public BambooBuildAdapterIdea(BambooBuild build) {
		super(build);
	}

	public Icon getBuildIcon() {
		if (build.getEnabled()) {
			switch (build.getStatus()) {
				case BUILD_FAILED:
					return ICON_RED;
				case UNKNOWN:
					return ICON_GREY;
				case BUILD_SUCCEED:
					return ICON_GREEN;
				default:
					throw new IllegalArgumentException("Illegal state of build.");
			}
		} else {
			return ICON_GREY;
		}
	}

}
