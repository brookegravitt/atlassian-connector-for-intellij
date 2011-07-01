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

package com.atlassian.theplugin.idea.action.bamboo;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.idea.Constants;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public abstract class AbstractBambooBuildAction extends AnAction {
	protected BambooBuild build;

	@Override
	public void update(final AnActionEvent event) {
		build = getBuild(event);
		boolean enabled = false;
		if (build != null) {
			if (build.getPlanKey() != null && build.isValid()) {
				enabled = true;
			}
		}
		event.getPresentation().setEnabled(enabled);
	}

	protected BambooBuild getBuild(final AnActionEvent event) {
		return (BambooBuild) event.getDataContext().getData(Constants.BAMBOO_BUILD_KEY.getName());
	}
}