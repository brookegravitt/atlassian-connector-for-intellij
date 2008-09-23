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

package com.atlassian.theplugin.idea.action.tree.file;

import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.CrucibleReviewWindow;
import com.atlassian.theplugin.idea.IdeaHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 30, 2008
 * Time: 4:18:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExpandCollapseFilesAction extends TreeAction {
	protected void executeTreeAction(final Project project, AtlassianTreeWithToolbar tree) {
		if (tree == null) {
			tree = CrucibleReviewWindow.getInstance(project).getAtlassianTreeWithToolbar();
		}

		if (tree != null) {
			tree.setViewState(tree.getViewState().getNextState());
		}
	}

	protected void updateTreeAction(final AnActionEvent e, AtlassianTreeWithToolbar tree) {
		if (tree == null) {
			Project project = IdeaHelper.getCurrentProject(e);
			if (project != null) {
				tree = CrucibleReviewWindow.getInstance(project).getAtlassianTreeWithToolbar();
			}
		}

		if (tree != null) {
			e.getPresentation().setIcon(tree.getViewState().getNextState().getIcon());
			e.getPresentation().setText(tree.getViewState().getNextState().toString());
			e.getPresentation().setEnabled(!tree.isEmpty());
		}
	}
}
