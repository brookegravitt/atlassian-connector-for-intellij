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
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.idea.bamboo.BuildListModel;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public class PlanAndBranchBuildNodeManipulator extends ProjectBuildNodeManipulator {
	public PlanAndBranchBuildNodeManipulator(final BuildListModel buildModel, final DefaultMutableTreeNode root) {
		super(buildModel, root);
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == rootNode) {
			return getDistinctProjects().size();
		} else if (parent instanceof BuildProjectTreeNode) {
			BuildProjectTreeNode projectNode = (BuildProjectTreeNode) parent;
			return getNumOfPlansPerProject(projectNode.getProject());
		} else if (parent instanceof BuildPlanTreeNode) {
            BuildPlanTreeNode planNode = (BuildPlanTreeNode) parent;
            return getNumOfBranchesPerPlan(planNode.getPlanKey());
        }

		return 0;
	}

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        DefaultMutableTreeNode p = (DefaultMutableTreeNode) parent;

        for (int i = 0; i < p.getChildCount(); ++i) {
            if (((ComparingTreeNode) p.getChildAt(i)).isMe(child)) {
                return i;
            }
        }
        return -1;
    }

    @Override
	public Object getChild(Object parent, int index) {
		if (parent == rootNode) {
            return super.getChild(parent, index);
		} else if (parent instanceof BuildProjectTreeNode) {
			BuildProjectTreeNode p = (BuildProjectTreeNode) parent;

			if (index < p.getChildCount()) {
				return p.getChildAt(index);
			}

			BambooPlan plan = getPlanForProject(p.getProject(), index);
            BuildPlanTreeNode node = new BuildPlanTreeNode(plan.getKey(), plan.getName());
			p.add(node);

			return node;
		} else if (parent instanceof BuildPlanTreeNode) {
            BuildPlanTreeNode p = (BuildPlanTreeNode) parent;
            if (index < p.getChildCount()) {
                return p.getChildAt(index);
            }
            BambooBuildAdapter build = getBuildForPlan(p.getPlanKey(), index);
            BuildTreeNode node = new BuildTreeNode(buildModel, build);
            p.add(node);

            return node;
        }

		return null;
	}

	private int getNumOfPlansPerProject(String projectName) {
        int ret = 0;
        for (BambooBuildAdapter build : buildModel.getBuilds()) {
            if (build.getMasterPlanKey() != null) {
                continue;
            }
            if (build.getProjectName().equals(projectName)) {
                ++ret;
            }
        }

        return ret;
    }

    private int getNumOfBranchesPerPlan(String planKey) {
        int ret = 0;
        for (BambooBuildAdapter build : buildModel.getBuilds()) {
            String name = build.getMasterPlanKey() != null ? build.getMasterPlanKey() : build.getPlanKey();
            if (name.equals(planKey)) {
                ++ret;
            }
        }

        return ret;
    }

    private BambooPlan getPlanForProject(String projectName, int index) {
		List<BambooPlan> array = new ArrayList<BambooPlan>();

        for (BambooBuildAdapter build : buildModel.getBuilds()) {
            if (build.getMasterPlanKey() != null) {
                continue;
            }
            if (build.getProjectName().equals(projectName)) {
                array.add(new BambooPlan(build.getPlanName(), build.getPlanKey(), null));
            }
        }

		return array.get(index);
    }

    private BambooBuildAdapter getBuildForPlan(String planKey, int index) {
		List<BambooBuildAdapter> array = new ArrayList<BambooBuildAdapter>();

        BambooBuildAdapter master = null;
        for (BambooBuildAdapter build : buildModel.getBuilds()) {
            if (build.getMasterPlanKey() != null) {
                continue;
            }
            if (build.getPlanKey().equals(planKey)) {
                master = build;
            }
        }
        if (master != null) {
            array.add(master);
        }
		for (BambooBuildAdapter build : buildModel.getBuilds()) {
			if (build.getMasterPlanKey() != null && build.getMasterPlanKey().equals(planKey)) {
				array.add(build);
			}
		}

		return index < array.size() ? array.get(index) : null;
	}
}
