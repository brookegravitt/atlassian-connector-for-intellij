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

import com.atlassian.theplugin.idea.IdeaHelper;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 30, 2008
 * Time: 4:18:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToggleViewAction extends TreeAction {

	protected void executeTreeAction(final Project project, AtlassianTreeWithToolbar tree) {
		if (tree == null) {
			tree = IdeaHelper.getReviewDetailsToolWindow(project).getAtlassianTreeWithToolbar();
		}
		if (tree != null) {
			tree.changeState();
			tree.setViewState(AtlassianTreeWithToolbar.ViewState.EXPANDED);
		}
	}

	protected void updateTreeAction(final AnActionEvent e, final AtlassianTreeWithToolbar tree) {
		if (tree != null) {
			e.getPresentation().setIcon(tree.getState().getNextState().getIcon());
			e.getPresentation().setText(tree.getState().getNextState().toString());
		}
	}
}
