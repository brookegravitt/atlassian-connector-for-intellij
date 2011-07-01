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

import com.intellij.openapi.actionSystem.AnActionEvent;

public abstract class AbstractBamboo2BuildAction extends AbstractBambooBuildAction {
	@Override
	public void update(final AnActionEvent event) {
		super.update(event);
		//todo: implement isBamboo2
		if (build != null /*&& !build.getJiraServerData().isBamboo2()*/) {
			event.getPresentation().setEnabled(false);
		}
	}
}