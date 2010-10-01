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
import com.atlassian.theplugin.idea.bamboo.BuildGroupBy;
import com.atlassian.theplugin.idea.bamboo.BuildListModelListener;
import com.atlassian.theplugin.idea.ui.tree.AbstractTree;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeRenderer;
import com.atlassian.theplugin.idea.ui.tree.paneltree.TreeUISetup;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Jacek Jaroczynski
 */
public class BuildTree extends AbstractTree {
    private BuildTreeModel buildTreeModel;
    private final TreeUISetup buildTreeUiSetup;
    private static final TreeCellRenderer TREE_RENDERER = new TreeRenderer();

    public BuildTree(final BuildGroupBy groupBy, final BuildTreeModel buildTreeModel,
                     @NotNull final JScrollPane parentScrollPane) {
        super(buildTreeModel);

        this.buildTreeModel = buildTreeModel;
        this.buildTreeModel.setGroupBy(groupBy);
        buildTreeUiSetup = new TreeUISetup(TREE_RENDERER);
        buildTreeUiSetup.initializeUI(this, parentScrollPane);

        init();

        buildTreeModel.addTreeModelListener(new LocalTreeModelListener());
        buildTreeModel.getBuildListModel().addListener(new LocalBuildListModelListener());

        addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(final MouseEvent e) {
            }

            public void mouseMoved(final MouseEvent e) {
                TreePath path = getPathForLocation(e.getX(), e.getY());
                if (path != null) {
                    if (path.getLastPathComponent() instanceof BuildTreeNode) {
                        BuildTreeNode node = (BuildTreeNode) path.getLastPathComponent();
                        buildTreeModel.setHoeverNode(node);
                    } else {
                        buildTreeModel.setHoeverNode(null);
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseExited(final MouseEvent e) {
                buildTreeModel.setHoeverNode(null);
            }
        });
    }

    private void init() {
        setRootVisible(false);
        setShowsRootHandles(true);
        setExpandsSelectedPaths(true);
        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    }


    @Nullable
    public BambooBuildAdapter getSelectedBuild() {
        final List<BambooBuildAdapter> selectedBuilds = getSelectedBuilds();
        if (selectedBuilds != null && selectedBuilds.size() == 1) {
            return selectedBuilds.get(0);
        }

        return null;
    }
    @Nullable
    public List<BambooBuildAdapter> getSelectedBuilds() {
        final TreePath[] selectionPaths = getSelectionPaths();
        List<BambooBuildAdapter> selectedBuilds = null;
        if (selectionPaths != null && selectionPaths.length > 0) {
            selectedBuilds = new ArrayList<BambooBuildAdapter>();
            for (TreePath tp : selectionPaths) {
                if (tp.getLastPathComponent() != null && tp.getLastPathComponent() instanceof BuildTreeNode) {
                    selectedBuilds.add(((BuildTreeNode) tp.getLastPathComponent()).getBuild());
                }
            }
        }
        return selectedBuilds;
    }

    private void selectBuildNode(BambooBuildAdapter build) {
        if (build == null) {
            clearSelection();
            return;
        }

        for (int i = 0; i < getRowCount(); i++) {
            TreePath path = getPathForRow(i);
            Object object = path.getLastPathComponent();
            if (object instanceof BuildTreeNode) {
                BuildTreeNode node = (BuildTreeNode) object;
                if (node.getBuild().getPlanKey().equals(build.getPlanKey())) {
                    expandPath(path);
                    makeVisible(path);
                    setSelectionPath(path);
                    break;
                }
            }
        }
    }

    public void groupBy(final BuildGroupBy groupingType) {
        buildTreeModel.groupBy(groupingType);
        expandTree();
    }

    private class LocalTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(final TreeModelEvent e) {
        }

        public void treeNodesInserted(final TreeModelEvent e) {
        }

        public void treeNodesRemoved(final TreeModelEvent e) {
        }

        public void treeStructureChanged(final TreeModelEvent e) {
//			expandTree();
        }
    }

    private class LocalBuildListModelListener implements BuildListModelListener {

        public void modelChanged() {
            refreshTree();
        }

        public void buildsChanged(@Nullable final Collection<String> additionalInfo,
                                  @Nullable final Collection<Pair<String, Throwable>> errors) {
            refreshTree();
        }

        public void generalProblemsHappened(@Nullable Collection<Exception> generalExceptions) {
        }

        private void refreshTree() {
            //		long begin = System.currentTimeMillis();
            try {
                buildTreeUiSetup.setTreeRebuilding(true);
                Set<TreePath> collapsedPaths = getCollapsedPaths();
				BambooBuildAdapter build = getSelectedBuild();

                // rebuild the tree
                buildTreeModel.update();

                // expand entire tree
                expandTree();

                // restore selection and collapse state
                collapsePaths(collapsedPaths);
				selectBuildNode(build);
            } finally {
                buildTreeUiSetup.setTreeRebuilding(false);
                buildTreeUiSetup.forceTreePrefSizeRecalculation(BuildTree.this);
            }
            //			System.out.println("Time: " + (System.currentTimeMillis() - begin));
        }
    }
}
