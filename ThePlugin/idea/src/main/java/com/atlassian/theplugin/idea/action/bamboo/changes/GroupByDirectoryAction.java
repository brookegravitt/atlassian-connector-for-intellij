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

package com.atlassian.theplugin.idea.action.bamboo.changes;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.atlassian.theplugin.idea.bamboo.BuildChangesToolWindow;

public class GroupByDirectoryAction extends ToggleAction {
	public boolean isSelected(AnActionEvent event) {
		BuildChangesToolWindow.ChangesTree tree = BuildChangesToolWindow.getChangesTree(event.getPlace());
		if (tree == null) {
			return !BuildChangesToolWindow.ChangesTree.GROUP_BY_DIRECTORY_DEFAULT;
		}
		return tree.isGroupByDirectory();
	}

	public void setSelected(AnActionEvent event, boolean b) {
		BuildChangesToolWindow.ChangesTree tree = BuildChangesToolWindow.getChangesTree(event.getPlace());
		tree.setGroupByDirectory(b);
	}
}
