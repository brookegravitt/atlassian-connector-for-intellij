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

import com.atlassian.theplugin.idea.bamboo.BuildListModel;
import org.apache.commons.lang.NotImplementedException;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Jacek Jaroczynski
 */
public abstract class BuildNodeManipulator {

	protected final BuildListModel buildModel;
	protected final DefaultMutableTreeNode rootNode;

	public BuildNodeManipulator(final BuildListModel buildModel, final DefaultMutableTreeNode rootNode) {
		this.buildModel = buildModel;
		this.rootNode = rootNode;
	}

	public abstract int getChildCount(Object parent);

	public abstract Object getChild(Object parent, int index);

    public int getIndexOfChild(Object parent, Object child) {
        throw new NotImplementedException("Well, damn. Something went wrong");
    }
}
