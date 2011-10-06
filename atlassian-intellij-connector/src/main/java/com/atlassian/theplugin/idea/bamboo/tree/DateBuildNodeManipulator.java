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
import java.util.*;

/**
 * @author Jacek Jaroczynski
 */
public class DateBuildNodeManipulator extends BuildNodeManipulator {
	public DateBuildNodeManipulator(final BuildListModel buildModel, final DefaultMutableTreeNode root) {
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

			DatePeriod date = getDistinctDates().get(index);

			BuildDateTreeNode dateNode = new BuildDateTreeNode(date);
			p.add(dateNode);

			return dateNode;

		} else if (parent instanceof BuildDateTreeNode) {
			BuildDateTreeNode p = (BuildDateTreeNode) parent;

			if (index < p.getChildCount()) {
				return p.getChildAt(index);
			}

			BambooBuildAdapter build = getBuildForDate(p.getDate(), index);
			BuildTreeNode node = new BuildTreeNode(buildModel, build);
			p.add(node);

			return node;
		}

		return null;
	}

	private List<DatePeriod> getDistinctDates() {
		Set<DatePeriod> dates = new TreeSet<DatePeriod>(COMPARATOR);

		for (BambooBuildAdapter build : buildModel.getBuilds()) {
			dates.add(DatePeriod.getBuilDate(build.getCompletionDate()));
		}

		return new ArrayList<DatePeriod>(dates);
	}

	private static final Comparator<DatePeriod> COMPARATOR = new Comparator<DatePeriod>() {
		public int compare(DatePeriod lhs, DatePeriod rhs) {
			return lhs.ordinal() - rhs.ordinal();
		}
	};

	private int gentNumOfBuildsForDate(DatePeriod date) {
		int ret = 0;
		for (BambooBuildAdapter build : buildModel.getBuilds()) {
			if (DatePeriod.getBuilDate(build.getCompletionDate()) == date) {
				++ret;
			}
		}

		return ret;
	}

	private BambooBuildAdapter getBuildForDate(DatePeriod date, int index) {
		List<BambooBuildAdapter> array = new ArrayList<BambooBuildAdapter>();

		// get all builds for date
		for (BambooBuildAdapter build : buildModel.getBuilds()) {
			if (DatePeriod.getBuilDate(build.getCompletionDate()) == date) {
				array.add(build);
			}
		}

		return array.get(index);
	}
}
