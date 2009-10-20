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

import com.atlassian.theplugin.idea.Constants;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 31, 2008
 * Time: 8:35:51 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class TreeAction extends AnAction {

	@Override
	public void update(final AnActionEvent e) {
		AtlassianTreeWithToolbar tree = identifyTreeWithAllPossibleMeans(e);
		boolean enabled = true;

		e.getPresentation().setEnabled(enabled);

		updateTreeAction(e, tree);
	}

	public void actionPerformed(final AnActionEvent e) {
		AtlassianTreeWithToolbar tree = identifyTreeWithAllPossibleMeans(e);
		Project project = DataKeys.PROJECT.getData(e.getDataContext());
		executeTreeAction(project, tree);
	}

	private AtlassianTreeWithToolbar identifyTreeWithAllPossibleMeans(final AnActionEvent e) {
		AtlassianTreeWithToolbar tree = findTreeM1(e);
		if (tree == null) {
			tree = findTreeM2(e);
		}
		if (tree == null) {
			tree = findTreeM3(e);
		}
		return tree;
	}

	private AtlassianTreeWithToolbar findTreeM1(final AnActionEvent e) {
		return (AtlassianTreeWithToolbar) e.getDataContext().getData(Constants.FILE_TREE);
	}

	private AtlassianTreeWithToolbar findTreeM2(final AnActionEvent e) {
		AtlassianTreeWithToolbar tree = null;
		Component component = DataKeys.CONTEXT_COMPONENT.getData(e.getDataContext());
		if (component != null) {
			Container parent = component.getParent();
			while (parent != null) {
				if (parent instanceof DataProvider) {
					DataProvider o = (DataProvider) parent;
					tree = (AtlassianTreeWithToolbar) o.getData(Constants.FILE_TREE);
					if (tree != null) {
						break;
					}
				}
				parent = parent.getParent();
			}
		}
		return tree;
	}

	private AtlassianTreeWithToolbar findTreeM3(final AnActionEvent e) {
		Component component = DataKeys.CONTEXT_COMPONENT.getData(e.getDataContext());
		Container parent = null;
		if (component != null) {
			parent = component.getParent();
			while (parent != null) {
				if (parent instanceof AtlassianTreeWithToolbar) {
					break;
				}
				parent = parent.getParent();
			}
		}
		if (parent == null) {
			return null;
		}
		return (AtlassianTreeWithToolbar) parent;
	}


	protected abstract void executeTreeAction(@Nullable final Project project, AtlassianTreeWithToolbar tree);

	protected abstract void updateTreeAction(@Nullable final AnActionEvent e, final AtlassianTreeWithToolbar tree);

}
