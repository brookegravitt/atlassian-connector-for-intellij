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
import com.atlassian.theplugin.idea.bamboo.BuildModel;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jacek Jaroczynski
 */
public class DateBuildNodeManipulator extends BuildNodeManipulator {
	public DateBuildNodeManipulator(final BuildModel buildModel, final DefaultMutableTreeNode root) {
		super(buildModel, root);
	}


	@Override
	public int getChildCount(Object parent) {
		if (parent == rootNode) {
			return getDistinctDates().size();
		} else if (parent instanceof BuildDateTreeNode) {
			BuildDateTreeNode stateNode = (BuildDateTreeNode) parent;
			return gentNumOfBuildsForDate(stateNode.getDate());
		}

		return 0;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent == rootNode) {

			DefaultMutableTreeNode p = (DefaultMutableTreeNode) parent;

			if (index < p.getChildCount()) {
				return p.getChildAt(index);
			}

			BuildDate date = getDistinctDates().get(index);

			BuildDateTreeNode dateNode = new BuildDateTreeNode(date);
			p.add(dateNode);

			return dateNode;

		} else if (parent instanceof BuildDateTreeNode) {
			BuildDateTreeNode p = (BuildDateTreeNode) parent;

			if (index < p.getChildCount()) {
				return p.getChildAt(index);
			}

			BambooBuildAdapterIdea build = getBuildForDate(p.getDate(), index);
			BuildTreeNode node = new BuildTreeNode(build);
			p.add(node);

			return node;
		}

		return null;
	}

	private List<BuildDate> getDistinctDates() {
		Set<BuildDate> dates = new LinkedHashSet<BuildDate>();	// ordered set

		for (BambooBuildAdapterIdea build : buildModel.getBuilds()) {
			dates.add(BuildDate.getBuilDate(build.getBuildTime()));
		}

		return new ArrayList<BuildDate>(dates);
	}

	private int gentNumOfBuildsForDate(BuildDate date) {
		int ret = 0;
		for (BambooBuildAdapterIdea build : buildModel.getBuilds()) {
			if (BuildDate.getBuilDate(build.getBuildTime()) == date) {
				++ret;
			}
		}

		return ret;
	}

	private BambooBuildAdapterIdea getBuildForDate(BuildDate date, int index) {
		List<BambooBuildAdapterIdea> array = new ArrayList<BambooBuildAdapterIdea>();

		// get all builds for date
		for (BambooBuildAdapterIdea build : buildModel.getBuilds()) {
			if (BuildDate.getBuilDate(build.getBuildTime()) == date) {
				array.add(build);
			}
		}

		return array.get(index);
	}
}
