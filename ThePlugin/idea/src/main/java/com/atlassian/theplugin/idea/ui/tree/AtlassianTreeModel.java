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

package com.atlassian.theplugin.idea.ui.tree;

import javax.swing.tree.DefaultTreeModel;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 11, 2008
 * Time: 1:31:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class AtlassianTreeModel extends DefaultTreeModel {

	public AtlassianTreeModel(AtlassianTreeNode root) {
        super(root);
	}

	public AtlassianTreeNode locateNode(NodeSearchAlgorithm alg) {
		return AtlassianTreeModel.locateNode(getRoot(), alg);
	}

	public AtlassianTreeModel getFilteredModel(Filter filter) {
		AtlassianTreeNode root = getRoot();
		AtlassianTreeNode newRoot = root.filter(filter);
		if (newRoot == null) {
			newRoot = AtlassianTreeNode.EMPTY_NODE;
		}
		AtlassianTreeModel result = new AtlassianTreeModel(newRoot);
		return result;
	}

	public AtlassianTreeNode getRoot() {
		return (AtlassianTreeNode) super.getRoot();
	}


	private static AtlassianTreeNode locateNode(AtlassianTreeNode startingNode, NodeSearchAlgorithm alg) {
		if (alg.check(startingNode)) {
			return startingNode;
		}
		for (int i = 0; i < startingNode.getChildCount(); i++) {
			AtlassianTreeNode result = locateNode(startingNode.getChildAt(i), alg);
			if (result != null) {
				return result;
			}
		}
		return null;
	}


}
