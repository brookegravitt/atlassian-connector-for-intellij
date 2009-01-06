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

import com.atlassian.theplugin.idea.bamboo.BambooBuildAdapterIdea;

import javax.swing.*;

/**
 * @author Jacek Jaroczynski
 */
public class BuildTreeNode extends AbstractBuildTreeNode {

	private BambooBuildAdapterIdea build;

	public BuildTreeNode(final BambooBuildAdapterIdea build) {
		super(build.getBuildKey(), null, null);

		this.build = build;
	}

	public BambooBuildAdapterIdea getBuild() {
		return build;
	}

	@Override
	public String toString() {
		return build.getBuildKey();
	}

	public JComponent getRenderer(final JComponent c, final boolean selected, final boolean expanded, final boolean hasFocus) {
		return null;
	}

	public void onSelect() {

	}
}
