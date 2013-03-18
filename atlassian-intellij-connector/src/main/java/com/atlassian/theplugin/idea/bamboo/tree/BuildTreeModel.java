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

import com.atlassian.connector.cfg.ProjectCfgManager;
import com.atlassian.connector.intellij.bamboo.BambooBuildAdapter;
import com.atlassian.theplugin.commons.bamboo.PlanState;
import com.atlassian.theplugin.idea.bamboo.BuildGroupBy;
import com.atlassian.theplugin.idea.bamboo.BuildListModel;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Jacek Jaroczynski
 */
public class BuildTreeModel extends DefaultTreeModel {

    private final BuildListModel buildListModel;

    private BuildGroupBy groupBy = BuildGroupBy.PLAN_AND_BRANCH;

    private final BuildNodeManipulator generalNodeManipulator;
    private final BuildNodeManipulator stateNodeManipulator;
    private final BuildNodeManipulator serverNodeManipulator;
    private final BuildNodeManipulator dateNodeManipulator;
    private final BuildNodeManipulator projectNodeManipulator;
    private final BuildNodeManipulator planAndBranchNodeManipulator;

    public BuildTreeModel(ProjectCfgManager projectCfgManager, final BuildListModel buildListModel) {
        super(new DefaultMutableTreeNode());

        this.buildListModel = buildListModel;

        generalNodeManipulator = new GeneralBuildNodeManipulator(buildListModel, getRoot());
        stateNodeManipulator = new StateBuildNodeManipulator(buildListModel, getRoot());
        serverNodeManipulator = new ServerBuildNodeManipulator(projectCfgManager, buildListModel, getRoot());
        dateNodeManipulator = new DateBuildNodeManipulator(buildListModel, getRoot());
        projectNodeManipulator = new ProjectBuildNodeManipulator(buildListModel, getRoot());
        planAndBranchNodeManipulator = new PlanAndBranchBuildNodeManipulator(buildListModel, getRoot());
    }

    /**
     * Sets groupBy field used to group the tree and triggers tree to rebuild
     * Only tree should use that method.
     *
     * @param aGroupBy group by option
     */
    public void groupBy(BuildGroupBy aGroupBy) {
        setGroupBy(aGroupBy);

        // clear entire tree
        getRoot().removeAllChildren();

        // redraw tree
        nodeStructureChanged(getRoot());
    }

    /**
     * Simple setter (does not trigger tree to rebuild)
     * Used when initializig tree (before first load of builds status)
     *
     * @param groupBy group by option
     */
    public void setGroupBy(BuildGroupBy groupBy) {
        if (groupBy != null) {
            this.groupBy = groupBy;
        }
    }

    /*
     Override TreeModel methods
      */

    @Override
    public DefaultMutableTreeNode getRoot() {
        return (DefaultMutableTreeNode) super.getRoot();
    }

    @Override
    public Object getChild(Object parent, int index) {

        switch (groupBy) {
            case DATE:
                return dateNodeManipulator.getChild(parent, index);
            case SERVER:
                return serverNodeManipulator.getChild(parent, index);
            case STATE:
                return stateNodeManipulator.getChild(parent, index);
            case PROJECT:
                return projectNodeManipulator.getChild(parent, index);
            case NONE:
                return generalNodeManipulator.getChild(parent, index);
            case PLAN_AND_BRANCH:
            default:
                return planAndBranchNodeManipulator.getChild(parent, index);
        }
    }

    @Override
    public int getChildCount(Object parent) {

        switch (groupBy) {
            case DATE:
                return dateNodeManipulator.getChildCount(parent);
            case SERVER:
                return serverNodeManipulator.getChildCount(parent);
            case STATE:
                return stateNodeManipulator.getChildCount(parent);
            case PROJECT:
                return projectNodeManipulator.getChildCount(parent);
            case NONE:
                return generalNodeManipulator.getChildCount(parent);
            case PLAN_AND_BRANCH:
            default:
                return planAndBranchNodeManipulator.getChildCount(parent);
        }
    }

    @Override
    public boolean isLeaf(Object node) {
        if (node == getRoot()
                || node instanceof BuildProjectTreeNode
                || node instanceof BuildStateTreeNode
                || node instanceof BuildDateTreeNode
                || node instanceof BuildServerTreeNode
                || node instanceof BuildPlanTreeNode) {
            return false;
        }

        return true;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        System.out.println("valueForPathChanged");
    }

    @Override
    // todo add group by handling if necessary
    public int getIndexOfChild(Object parent, Object child) {
        if (groupBy == BuildGroupBy.PLAN_AND_BRANCH) {
            return planAndBranchNodeManipulator.getIndexOfChild(parent, child);
        }

        if (parent == getRoot()) {
            if (child instanceof BuildTreeNode) {
                BambooBuildAdapter build = ((BuildTreeNode) child).getBuild();
                return new ArrayList<BambooBuildAdapter>(buildListModel.getBuilds()).indexOf(build);
            }
        }
        return -1;
    }

    Collection<BuildTreeNode> getBuildingNodes
            () {
        Collection<BuildTreeNode> nodes = new ArrayList<BuildTreeNode>();

        collectBuildingNodes(getRoot(), nodes);

        return nodes;
    }

    private void collectBuildingNodes(final DefaultMutableTreeNode node, Collection<BuildTreeNode> nodes) {
        if (node instanceof BuildTreeNode) {
            BuildTreeNode buildNode = (BuildTreeNode) node;
            if (buildNode.getBuild().getBuild().getPlanState() == PlanState.BUILDING) {
                nodes.add((BuildTreeNode) node);
            }
        }

        for (int i = 0; i < getChildCount(node); ++i) {
            if (getChild(node, i) instanceof DefaultMutableTreeNode) {
                collectBuildingNodes((DefaultMutableTreeNode) getChild(node, i), nodes);
            }
        }
    }

    public BuildListModel getBuildListModel() {
        return buildListModel;
    }

    /**
     * @param node Node to mark as hovered. Other nodes are cleared. If null all nodes are cleared.
     */
    public void setHoeverNode(final BuildTreeNode node) {
        Collection<DefaultMutableTreeNode> changedNodes = clearHoverNodes(getRoot());
        for (DefaultMutableTreeNode changedNode : changedNodes) {
            nodeStructureChanged(changedNode);
        }
        if (node != null) {
            node.setHover(true);
            nodeStructureChanged(node);
        }
    }

    private Collection<DefaultMutableTreeNode> clearHoverNodes(final DefaultMutableTreeNode node) {
        Collection<DefaultMutableTreeNode> ret = new ArrayList<DefaultMutableTreeNode>();
        if (node instanceof BuildTreeNode) {
            BuildTreeNode buildNode = (BuildTreeNode) node;
            if (buildNode.isHover()) {
                buildNode.setHover(false);
                ret.add(node);
            }
        }

        for (int i = 0; i < getChildCount(node); ++i) {
            if (getChild(node, i) instanceof DefaultMutableTreeNode) {
                ret.addAll(clearHoverNodes((DefaultMutableTreeNode) getChild(node, i)));
            }
        }

        return ret;
    }
}
