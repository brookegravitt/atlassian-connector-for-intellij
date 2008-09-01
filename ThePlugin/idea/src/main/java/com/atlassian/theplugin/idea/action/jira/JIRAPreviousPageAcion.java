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

package com.atlassian.theplugin.idea.action.jira;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;

public class JIRAPreviousPageAcion extends AnAction {
	private static final String DEFAULT_ICON = "/actions/back.png";

	public JIRAPreviousPageAcion() {
		getTemplatePresentation().setIcon(IconLoader.getIcon(DEFAULT_ICON));
		getTemplatePresentation().setText("Previous Results Page");
	}
	public void actionPerformed(AnActionEvent event) {
		IdeaHelper.getJIRAToolWindowPanel(event).prevPage();
	}

	public void update(AnActionEvent event) {
		super.update(event);
		if (IdeaHelper.getJIRAToolWindowPanel(event) != null) {
			event.getPresentation().setEnabled(IdeaHelper.getJIRAToolWindowPanel(event).isPrevPageAvailable());
		}
	}
}
