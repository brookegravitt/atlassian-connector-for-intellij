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
package com.atlassian.theplugin.idea.action.builds;

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.bamboo.BambooToolWindowPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author Jacek Jaroczynski
 */
public class CollapseAllAction extends AnAction {

	@Override
	public void actionPerformed(final AnActionEvent e) {
		final BambooToolWindowPanel panel = IdeaHelper.getProjectComponent(e, BambooToolWindowPanel.class);
		if (panel != null) {
			panel.collapseAllRightTreeNodes();
		}
	}
}
