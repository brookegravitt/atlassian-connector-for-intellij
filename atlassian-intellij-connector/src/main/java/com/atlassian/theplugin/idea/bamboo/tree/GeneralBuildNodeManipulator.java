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
package com.atlassian.theplugin.idea.bamboo.tree;

import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.idea.bamboo.BuildListModel;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Jacek Jaroczynski
 */
public class GeneralBuildNodeManipulator extends BuildNodeManipulator {

	public GeneralBuildNodeManipulator(BuildListModel buildModel, DefaultMutableTreeNode root) {
		super(buildModel, root);
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == rootNode) {
			return buildModel.getBuilds().size();
		}
		return 0;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent == rootNode) {
			BambooBuildAdapter build = (BambooBuildAdapter) buildModel.getBuilds().toArray()[index];
			if (build != null) {
				DefaultMutableTreeNode p = (DefaultMutableTreeNode) parent;
				if (index < p.getChildCount()) {
					return p.getChildAt(index);
				}

				AbstractBuildTreeNode n = new BuildTreeNode(buildModel, build);
				p.add(n);
				return n;
			}
		}

		return null;
	}
}
