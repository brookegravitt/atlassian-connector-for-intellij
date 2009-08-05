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

package com.atlassian.theplugin.idea.action.crucible;

import com.atlassian.connector.intellij.crucible.ReviewAdapter;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleFileInfo;
import com.atlassian.theplugin.idea.action.tree.file.TreeAction;
import com.atlassian.theplugin.idea.crucible.tree.AtlassianTreeWithToolbar;
import com.atlassian.theplugin.idea.ui.tree.AtlassianTreeNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleChangeSetTitleNode;
import com.atlassian.theplugin.idea.ui.tree.file.CrucibleFileNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import javax.swing.tree.TreeNode;

public abstract class ReviewTreeAction extends TreeAction {

	protected abstract void executeTreeAction(final Project project, final AtlassianTreeWithToolbar tree);

	protected void updateTreeAction(final AnActionEvent e, final AtlassianTreeWithToolbar tree) {
		ReviewActionData actionData = new ReviewActionData(tree);
		if (actionData.review != null && actionData.file != null) {
			e.getPresentation().setEnabled(true);
		} else {
			e.getPresentation().setEnabled(false);
		}
	}

	protected static class ReviewActionData {
		protected final ReviewAdapter review;
		protected CrucibleFileInfo file;

		ReviewActionData(AtlassianTreeWithToolbar tree) {
			AtlassianTreeNode node = tree != null ? tree.getSelectedTreeNode() : null;
			if (node != null) {
                while (node != null) {
                    if (node instanceof CrucibleFileNode) {
                        file = ((CrucibleFileNode) node).getFile();
                        break;
                    } else {
                        node = (AtlassianTreeNode) node.getParent();
                        file = null;
                    }
                }
				ReviewAdapter rd = null;
                if (node != null) {
                    for (TreeNode n : node.getPath()) {
                        if (n instanceof CrucibleChangeSetTitleNode) {
                            rd = ((CrucibleChangeSetTitleNode) n).getReview();
                            break;
                        }
                    }
                }
				review = rd;
			} else {
				review = null;
				file = null;
			}
		}
	}
}