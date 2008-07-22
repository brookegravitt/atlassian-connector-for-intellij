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

import com.intellij.ui.ColoredTreeCellRenderer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.MutableTreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: lguminski
 * Date: Jul 11, 2008
 * Time: 1:22:36 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AtlassianTreeNode extends DefaultMutableTreeNode {
	private AtlassianClickAction action;

	protected AtlassianTreeNode(AtlassianClickAction action) {
		super();
		this.action = action;
	}

	public void addNode(AtlassianTreeNode newChild) {
		super.add(newChild);	//To change body of overridden methods use File | Settings | File Templates.
	}

	public abstract TreeCellRenderer getTreeCellRenderer();

	public AtlassianClickAction getAtlassianClickAction() {
		return action;
	}

}
